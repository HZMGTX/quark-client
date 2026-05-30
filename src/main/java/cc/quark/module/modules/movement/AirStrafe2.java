package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * AirStrafe2 - Improved air strafing that applies directional acceleration
 * based on key inputs while airborne. Distinct from AirStrafe which uses EventMove.
 */
public class AirStrafe2 extends Module {

    private final DoubleSetting factor = register(new DoubleSetting("Factor", "Directional acceleration per tick", 0.025, 0.005, 0.1));
    private final BoolSetting onlyAir = register(new BoolSetting("OnlyAir", "Only apply while fully airborne", true));

    public AirStrafe2() {
        super("AirStrafe2", "Improved directional control while airborne via velocity acceleration", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean inAir = !mc.player.isOnGround();
        if (onlyAir.isEnabled() && !inAir) return;
        if (!inAir) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        double yawRad = Math.toRadians(mc.player.getYaw());
        double accel = factor.get();

        double dx = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * accel;
        double dz = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * accel;

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x + dx, vel.y, vel.z + dz);
    }
}
