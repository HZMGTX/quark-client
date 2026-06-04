package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class SneakFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Fly speed in all directions", 0.15, 0.01, 2.0));

    public SneakFly() {
        super("SneakFly", "Fly downward when sneaking, up when jumping", Category.MOVEMENT);
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

        double s = speed.get();
        float yaw = mc.player.getYaw();
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        double yawRad = Math.toRadians(yaw);
        double motX = 0, motZ = 0;
        double motY = 0;

        if (fwd != 0 || side != 0) {
            double len = Math.sqrt(fwd * fwd + side * side);
            motX = (-Math.sin(yawRad) * (fwd / len) + Math.cos(yawRad) * (side / len)) * s;
            motZ = (Math.cos(yawRad) * (fwd / len) + Math.sin(yawRad) * (side / len)) * s;
        }

        // Sneak = fly down, Jump = fly up
        if (mc.options.sneakKey.isPressed()) {
            motY = -s;
        } else if (mc.options.jumpKey.isPressed()) {
            motY = s;
        }

        mc.player.setVelocity(motX, motY, motZ);
        mc.player.fallDistance = 0.0f;
    }
}
