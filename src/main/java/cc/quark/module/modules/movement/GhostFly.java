package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class GhostFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Fly speed in blocks/tick", 0.3, 0.05, 2.0));

    private final BoolSetting silent = register(new BoolSetting(
            "Silent", "Spoof on-ground flag to server to avoid kick", true));

    public GhostFly() {
        super("GhostFly", "Creative-like fly without sending fly packets", Category.MOVEMENT);
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

        double s = speed.get();
        float yaw = mc.player.getYaw();
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        double yawRad = Math.toRadians(yaw);
        double dx = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * s;
        double dz = (Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * s;
        double dy = 0;

        if (mc.options.jumpKey.isPressed()) dy = s;
        else if (mc.options.sneakKey.isPressed()) dy = -s;

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(dx, dy != 0 ? dy : vel.y * 0.6, dz);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;
        if (silent.isEnabled()) {
            event.setOnGround(true);
        }
    }
}
