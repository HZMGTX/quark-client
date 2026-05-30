package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.util.math.Vec3d;

/**
 * RidingSpeed - when riding a horse, pig, or strider, applies extra velocity
 * to the entity in the player's facing direction each tick.
 */
public class RidingSpeed extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Extra velocity added per tick to the ridden animal", 0.2, 0.05, 1.0));

    public RidingSpeed() {
        super("RidingSpeed", "Extra speed when riding horses, pigs, or striders", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        Entity vehicle = mc.player.getVehicle();
        if (vehicle == null) return;

        // Only apply to horse, pig, or strider
        if (!(vehicle instanceof HorseBaseEntity)
                && !(vehicle instanceof PigEntity)
                && !(vehicle instanceof StriderEntity)) {
            return;
        }

        double yawRad = Math.toRadians(mc.player.getYaw());
        double spd = speed.get();

        double addX = -Math.sin(yawRad) * spd;
        double addZ =  Math.cos(yawRad) * spd;

        Vec3d vel = vehicle.getVelocity();
        vehicle.setVelocity(vel.x + addX, vel.y, vel.z + addZ);
    }
}
