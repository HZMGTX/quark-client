package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;

public class DurabilityAlert extends Module {

    private final IntSetting threshold = register(new IntSetting(
            "Threshold", "Alert when durability drops to or below this percentage", 20, 1, 100));

    private final BoolSetting sound = register(new BoolSetting(
            "Sound", "Play an alert sound when durability is low", true));

    private final BoolSetting chat = register(new BoolSetting(
            "Chat", "Show a chat warning when durability is low", true));

    private final TimerUtil timer = new TimerUtil();
    private static final long ALERT_COOLDOWN_MS = 10_000;

    public DurabilityAlert() {
        super("DurabilityAlert", "Alerts when armor/tool durability is low", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!timer.hasReached(ALERT_COOLDOWN_MS)) return;

        boolean alerted = false;

        // Check armor slots
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = mc.player.getEquippedStack(slot);
            if (checkAndAlert(stack, slot.getName())) {
                alerted = true;
            }
        }

        // Check main hand tool
        ItemStack mainHand = mc.player.getMainHandStack();
        if (checkAndAlert(mainHand, "main hand")) {
            alerted = true;
        }

        if (alerted) {
            if (sound.isEnabled() && mc.getSoundManager() != null) {
                mc.getSoundManager().play(
                        PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.3f, 0.7f));
            }
            timer.reset();
        }
    }

    private boolean checkAndAlert(ItemStack stack, String slotName) {
        if (stack.isEmpty()) return false;
        if (!stack.isDamageable()) return false;

        int maxDur = stack.getMaxDamage();
        if (maxDur <= 0) return false;

        int remaining = maxDur - stack.getDamage();
        int percent = (int) ((remaining / (float) maxDur) * 100);

        if (percent <= threshold.get()) {
            if (chat.isEnabled()) {
                ChatUtil.warn("[DurabilityAlert] " + stack.getName().getString()
                        + " (" + slotName + ") at " + percent + "% durability!");
            }
            return true;
        }
        return false;
    }
}
