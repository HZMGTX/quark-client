package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * FastElytra - applies extra forward velocity each tick when the player is
 * gliding with an elytra. Proportional to a SpeedMult setting.
 * Optionally auto-uses a firework rocket from inventory for a burst boost.
 */
public class FastElytra extends Module {

    private final DoubleSetting speedMult = register(new DoubleSetting(
            "SpeedMult", "Velocity multiplier applied each tick while gliding", 1.5, 1.0, 5.0));
    private final BoolSetting fireworkBoost = register(new BoolSetting(
            "FireworkBoost", "Auto-use a firework from inventory for burst boost", false));

    private int fireworkCooldown = 0;

    public FastElytra() {
        super("FastElytra", "Extra forward velocity while gliding with an elytra", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        fireworkCooldown = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isGliding()) return;

        // Apply extra forward velocity in look direction proportional to SpeedMult
        Vec3d look = mc.player.getRotationVector();
        Vec3d vel = mc.player.getVelocity();
        double mult = speedMult.get() * 0.05;

        mc.player.setVelocity(
                vel.x + look.x * mult,
                vel.y + look.y * mult,
                vel.z + look.z * mult);

        // Firework boost: auto-use a firework rocket when speed is low
        if (fireworkBoost.isEnabled() && fireworkCooldown <= 0) {
            double speed = vel.length();
            if (speed < 0.5) {
                int fireworkSlot = findFireworkSlot();
                if (fireworkSlot != -1 && mc.interactionManager != null) {
                    int prevSlot = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = fireworkSlot;
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    mc.player.getInventory().selectedSlot = prevSlot;
                    fireworkCooldown = 20;
                }
            }
        }

        if (fireworkCooldown > 0) fireworkCooldown--;
    }

    /**
     * Searches the hotbar for a firework rocket and returns its slot index, or -1.
     */
    private int findFireworkSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.FIREWORK_ROCKET)) return i;
        }
        return -1;
    }
}
