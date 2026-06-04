package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * GlideBoost - increases elytra glide speed by adding thrust in the look direction
 * each tick. Optionally auto-uses fireworks for periodic burst boosts.
 */
public class GlideBoost extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Thrust added per tick while gliding", 2.0, 0.1, 10.0));

    private final BoolSetting fireworks = register(new BoolSetting(
            "Fireworks", "Auto-use fireworks from hotbar while gliding", false));

    private int fireworkCooldown = 0;

    public GlideBoost() {
        super("GlideBoost", "Increase elytra glide speed", Category.MOVEMENT);
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
        double thrust   = speed.get() * 0.05;

        double lx = -Math.sin(yawRad)  * Math.cos(pitchRad) * thrust;
        double ly = -Math.sin(pitchRad) * thrust;
        double lz =  Math.cos(yawRad)  * Math.cos(pitchRad) * thrust;

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x + lx, vel.y + ly, vel.z + lz);

        // Auto fireworks
        if (fireworks.isEnabled() && fireworkCooldown <= 0) {
            int fireworkSlot = findFireworkSlot();
            if (fireworkSlot != -1) {
                int prevSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = fireworkSlot;
                if (mc.interactionManager != null) {
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                }
                mc.player.getInventory().selectedSlot = prevSlot;
                fireworkCooldown = 40;
            }
        } else if (fireworkCooldown > 0) {
            fireworkCooldown--;
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
