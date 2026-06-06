package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * EntityControl - provides improved control over ridden entities.
 *
 * Vanilla riding control is often stiff: animals can be steered only partially
 * and horses/pigs ignore direct directional input unless saddled correctly.
 * This module overrides the mounted entity's velocity each tick based on the
 * player's input and look direction, giving smooth, responsive steering.
 */
public class EntityControl extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Horizontal movement speed of the ridden entity", 0.25, 0.05, 2.0));
    private final DoubleSetting vertSpeed = register(new DoubleSetting(
            "Vert Speed", "Vertical speed when jumping/descending", 0.3, 0.05, 1.5));
    private final BoolSetting jumpControl = register(new BoolSetting(
            "Jump Control", "Use jump key to make the entity jump/ascend", true));
    private final BoolSetting sneakDescend = register(new BoolSetting(
            "Sneak Descend", "Use sneak key to descend while riding flying entities", false));
    private final BoolSetting noFallDamage = register(new BoolSetting(
            "No Fall Damage", "Reset fall distance of the vehicle each tick", true));

    public EntityControl() {
        super("EntityControl", "Better directional control over any ridden entity", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.hasVehicle()) return;

        Entity vehicle = mc.player.getVehicle();
        if (vehicle == null) return;

        float yawRad = (float) Math.toRadians(mc.player.getYaw());
        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        double vx = 0, vz = 0;
        if (fwd != 0 || side != 0) {
            double len = Math.sqrt(fwd * fwd + side * side);
            double nFwd  = fwd  / len;
            double nSide = side / len;
            vx = (-Math.sin(yawRad) * nFwd + Math.cos(yawRad) * nSide) * speed.get();
            vz = ( Math.cos(yawRad) * nFwd + Math.sin(yawRad) * nSide) * speed.get();
        }

        Vec3d cur = vehicle.getVelocity();
        double vy = cur.y;

        if (jumpControl.isEnabled() && mc.options.jumpKey.isPressed()) {
            vy = vertSpeed.get();
        } else if (sneakDescend.isEnabled() && mc.options.sneakKey.isPressed()) {
            vy = -vertSpeed.get();
        }

        vehicle.setVelocity(
                fwd != 0 || side != 0 ? vx : cur.x * 0.6,
                vy,
                fwd != 0 || side != 0 ? vz : cur.z * 0.6
        );

        if (noFallDamage.isEnabled()) {
            vehicle.fallDistance = 0;
        }
    }
}
