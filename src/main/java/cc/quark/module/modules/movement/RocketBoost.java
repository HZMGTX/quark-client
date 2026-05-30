package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * RocketBoost - when gliding with an elytra and vertical speed drops below
 * MinVSpeed, automatically uses a firework rocket to boost the player.
 * Uses Slot setting to specify a hotbar slot (-1 = auto-find).
 */
public class RocketBoost extends Module {

    private final DoubleSetting minVSpeed = register(new DoubleSetting(
            "MinVSpeed", "Vertical speed threshold that triggers a rocket boost", -0.5, -3.0, 0.0));
    private final IntSetting slot = register(new IntSetting(
            "Slot", "Hotbar slot for fireworks (-1 = auto-find)", -1, -1, 8));

    private int cooldown = 0;

    public RocketBoost() {
        super("RocketBoost", "Auto-fires firework rockets while elytra gliding when speed drops", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        cooldown = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!mc.player.isGliding()) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        double verticalSpeed = mc.player.getVelocity().y;
        if (verticalSpeed >= minVSpeed.get()) return;

        int targetSlot = slot.get();
        if (targetSlot == -1) {
            // Auto-find firework in hotbar
            targetSlot = findFireworkSlot();
        } else {
            // Verify the specified slot has a firework
            if (targetSlot >= 0 && targetSlot < 9) {
                ItemStack stack = mc.player.getInventory().getStack(targetSlot);
                if (!stack.isOf(Items.FIREWORK_ROCKET)) {
                    targetSlot = findFireworkSlot(); // fallback
                }
            }
        }

        if (targetSlot == -1) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = targetSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prevSlot;
        cooldown = 15;
    }

    private int findFireworkSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.FIREWORK_ROCKET)) return i;
        }
        return -1;
    }
}
