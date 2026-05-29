package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * BoatJump - when riding a boat and the space key is pressed, eject from the
 * boat and apply a strong upward velocity burst to launch the player into the
 * air.  Optionally keep horizontal boat momentum on ejection.
 */
public class BoatJump extends Module {

    private final DoubleSetting force = register(new DoubleSetting(
            "Force", "Upward velocity applied after boat ejection", 3.0, 0.5, 10.0));
    private final BoolSetting keepMomentum = register(new BoolSetting(
            "Keep Momentum", "Preserve horizontal boat velocity on ejection", true));
    private final BoolSetting resetFall = register(new BoolSetting(
            "Reset Fall", "Reset fall distance to prevent fall damage", true));

    public BoatJump() {
        super("BoatJump", "Eject from boat with a powerful upward launch on SPACE", Category.MOVEMENT);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (mc.player == null) return;
        if (event.getKeyCode() != GLFW.GLFW_KEY_SPACE) return;

        Entity vehicle = mc.player.getVehicle();
        if (!(vehicle instanceof BoatEntity boat)) return;

        Vec3d boatVel = boat.getVelocity();

        mc.player.stopRiding();

        double vx = keepMomentum.isEnabled() ? boatVel.x : 0;
        double vz = keepMomentum.isEnabled() ? boatVel.z : 0;
        mc.player.setVelocity(vx, force.get(), vz);

        if (resetFall.isEnabled()) {
            mc.player.fallDistance = 0;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        // Keep fall distance at zero while riding a boat so we don't take damage
        // from hitting the water after a jump
        if (mc.player.getVehicle() instanceof BoatEntity) {
            mc.player.fallDistance = 0;
        }
    }
}
