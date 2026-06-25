package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * ElytraBoost2 - provides constant thrust while gliding with an elytra,
 * removing the need to manually use firework rockets for sustained flight.
 *
 * <p>A small acceleration is added in the look direction every tick.
 * When {@code Auto Firework} is enabled, a firework rocket from the hotbar
 * is auto-used periodically for additional burst speed.
 */
public class ElytraBoost2 extends Module {

    private final DoubleSetting power = register(new DoubleSetting(
            "Power", "Constant thrust added per tick while gliding", 0.15, 0.01, 2.0));

    private final BoolSetting autoFirework = register(new BoolSetting(
            "Auto Firework", "Automatically use fireworks from hotbar", false));

    private int fireworkCooldown = 0;

    public ElytraBoost2() {
        super("ElytraBoost2", "Boost elytra with constant thrust", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        fireworkCooldown = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isFallFlying()) return;

        double yawRad   = Math.toRadians(mc.player.getYaw());
        double pitchRad = Math.toRadians(mc.player.getPitch());
        double thrust   = power.get() * 0.05;

        double lx = -Math.sin(yawRad)  * Math.cos(pitchRad) * thrust;
        double ly = -Math.sin(pitchRad) * thrust;
        double lz =  Math.cos(yawRad)  * Math.cos(pitchRad) * thrust;

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x + lx, vel.y + ly, vel.z + lz);

        // Auto firework
        if (autoFirework.isEnabled()) {
            if (fireworkCooldown > 0) {
                fireworkCooldown--;
            } else {
                int slot = findFireworkSlot();
                if (slot != -1) {
                    int prev = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = slot;
                    if (mc.interactionManager != null) {
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    }
                    mc.player.getInventory().selectedSlot = prev;
                    fireworkCooldown = 40;
                }
            }
        }
    }

    private int findFireworkSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.FIREWORK_ROCKET) {
                return i;
            }
        }
        return -1;
    }
}
