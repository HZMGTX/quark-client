package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * AirControl - improves steering while airborne by nudging velocity toward the
 * player's input direction.
 */
public class AirControl extends Module {

    private final DoubleSetting strength = register(new DoubleSetting(
            "Strength", "Air steering strength", 0.05, 0.0, 0.3));

    public AirControl() {
        super("AirControl", "Better air movement control", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;
        double yawRad = Math.toRadians(mc.player.getYaw());
        double x = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * strength.get();
        double z = (Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * strength.get();
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x + x, v.y, v.z + z);
    }
}
