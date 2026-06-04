package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class NoFallFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Horizontal fly speed", 0.15, 0.01, 2.0));

    private final DoubleSetting vertical = register(new DoubleSetting(
            "Vertical", "Vertical fly speed", 0.1, 0.01, 1.0));

    public NoFallFly() {
        super("NoFallFly", "Fly mode that cancels fall damage", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            mc.player.getAbilities().flying = false;
        }
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

        double hSpeed = speed.get();
        double vSpeed = vertical.get();
        float yaw = mc.player.getYaw();
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        double yawRad = Math.toRadians(yaw);
        double motX = 0, motZ = 0, motY = 0;

        if (fwd != 0 || side != 0) {
            double len = Math.sqrt(fwd * fwd + side * side);
            motX = (-Math.sin(yawRad) * (fwd / len) + Math.cos(yawRad) * (side / len)) * hSpeed;
            motZ = (Math.cos(yawRad) * (fwd / len) + Math.sin(yawRad) * (side / len)) * hSpeed;
        }

        if (mc.options.jumpKey.isPressed()) {
            motY = vSpeed;
        } else if (mc.options.sneakKey.isPressed()) {
            motY = -vSpeed;
        }

        mc.player.setVelocity(motX, motY, motZ);

        // Cancel fall damage by resetting fallDistance while flying
        mc.player.fallDistance = 0.0f;
    }
}
