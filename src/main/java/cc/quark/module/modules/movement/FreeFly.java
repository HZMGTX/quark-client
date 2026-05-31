package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class FreeFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Fly speed in all directions (blocks/tick)", 0.3, 0.05, 3.0));

    public FreeFly() {
        super("FreeFly", "Fly with full 6-DOF control; pitch determines vertical movement",
                Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.getAbilities().flying = false;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        mc.player.getAbilities().flying = false;
        mc.player.fallDistance = 0;

        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double s = speed.get();

        // Forward vector includes pitch for vertical component
        double cosP = Math.cos(pitchRad);
        double sinP = Math.sin(pitchRad);

        double dirFwdX = -Math.sin(yawRad) * cosP;
        double dirFwdY = -sinP;
        double dirFwdZ = Math.cos(yawRad) * cosP;

        double dirSideX = Math.cos(yawRad);
        double dirSideZ = Math.sin(yawRad);

        double dx = (dirFwdX * fwd + dirSideX * side) * s;
        double dy = dirFwdY * fwd * s;
        double dz = (dirFwdZ * fwd + dirSideZ * side) * s;

        // Strafe-only vertical override via jump/sneak
        if (mc.options.jumpKey.isPressed()) dy += s;
        if (mc.options.sneakKey.isPressed()) dy -= s;

        mc.player.setVelocity(dx, dy, dz);
    }
}
