package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;

public class DurabilityAlert extends Module {

    private final IntSetting threshold = register(new IntSetting(
            "Threshold", "Durability percentage below which to alert", 15, 1, 99));

    private final TimerUtil timer = new TimerUtil();

    public DurabilityAlert() {
        super("DurabilityAlert", "Alerts in chat when any equipped item durability drops below threshold", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!timer.hasReached(5000)) return;
        timer.reset();

        checkStack(mc.player.getMainHandStack(), "Main Hand");
        checkStack(mc.player.getOffHandStack(), "Off Hand");
        for (int i = 0; i < 4; i++) {
            checkStack(mc.player.getInventory().getArmorStack(i), armorSlotName(i));
        }
    }

    private void checkStack(ItemStack stack, String label) {
        if (stack.isEmpty() || stack.getMaxDamage() <= 0) return;
        int pct = (int) (((float) (stack.getMaxDamage() - stack.getDamage()) / stack.getMaxDamage()) * 100);
        if (pct <= threshold.get()) {
            ChatUtil.warn("[DurabilityAlert] " + label + " (" + stack.getName().getString() + ") is at " + pct + "% durability!");
        }
    }

    private String armorSlotName(int slot) {
        return switch (slot) {
            case 0 -> "Boots";
            case 1 -> "Leggings";
            case 2 -> "Chestplate";
            case 3 -> "Helmet";
            default -> "Armor";
        };
    }
}
