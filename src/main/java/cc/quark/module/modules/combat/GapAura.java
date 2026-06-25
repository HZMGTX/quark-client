package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class GapAura extends Module {

    private final DoubleSetting health = register(new DoubleSetting("Health", "Trigger health threshold", 5.0, 1.0, 10.0));
    private final BoolSetting notchOnly = register(new BoolSetting("Notch Only", "Only use enchanted golden apples", false));

    private final TimerUtil timer = new TimerUtil();
    private int previousSlot = -1;

    public GapAura() {
        super("GapAura", "Auto-eats golden apples when health is low during combat", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        previousSlot = -1;
        timer.reset();
    }

    @Override
    public void onDisable() {
        if (mc.player != null && previousSlot != -1) {
            mc.player.getInventory().selectedSlot = previousSlot;
        }
        previousSlot = -1;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(200)) return;

        boolean enemyNearby = false;
        for (net.minecraft.entity.Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof LivingEntity living)) continue;
            if (living.isRemoved()) continue;
            if (living instanceof PlayerEntity) continue;
            if (mc.player.distanceTo(e) <= 6.0) {
                enemyNearby = true;
                break;
            }
        }

        float hp = mc.player.getHealth();

        if (!enemyNearby || hp > (float) health.get()) {
            if (previousSlot != -1) {
                mc.player.getInventory().selectedSlot = previousSlot;
                previousSlot = -1;
            }
            return;
        }

        int slot = findGappleSlot();
        if (slot == -1) return;

        if (mc.player.getInventory().selectedSlot != slot) {
            if (previousSlot == -1) previousSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
        }

        if (!mc.player.isUsingItem()) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            timer.reset();
        }
    }

    private int findGappleSlot() {
        int regularSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) return i;
            if (!notchOnly.isEnabled() && stack.isOf(Items.GOLDEN_APPLE) && regularSlot == -1) {
                regularSlot = i;
            }
        }
        return regularSlot;
    }
}
