package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * CrouchFly - when sneaking while airborne, apply a slow controlled downward
 * velocity instead of free-fall acceleration.  Fall distance is reset to
 * prevent fall damage on landing.
 */
public class CrouchFly extends Module {

    private final DoubleSetting descendSpeed = register(new DoubleSetting(
            "Descend Speed", "Downward velocity while sneaking airborne (blocks/tick)", 0.1, 0.01, 1.0));
    private final DoubleSetting horizontalSpeed = register(new DoubleSetting(
            "Horizontal Speed", "Horizontal control speed while crouching in air", 0.15, 0.01, 0.5));
    private final BoolSetting noFallDamage = register(new BoolSetting(
            "No Fall Damage", "Reset fall distance so landing doesn't hurt", true));

    public CrouchFly() {
        super("CrouchFly", "Slow controlled descent when sneaking airborne", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isSneaking()) return;
        if (mc.player.isOnGround()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        double spd = horizontalSpeed.get();
        double yawRad = Math.toRadians(mc.player.getYaw());

        double dx = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * spd;
        double dz = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * spd;

        mc.player.setVelocity(dx, -descendSpeed.get(), dz);

        if (noFallDamage.isEnabled()) {
            mc.player.fallDistance = 0;
        }
    }
}
