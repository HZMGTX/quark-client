package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;

public class SurfRide extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to detect rideable mobs", 1.5, 0.5, 3.0));

    private Entity ridingTarget = null;

    public SurfRide() {
        super("SurfRide", "Ride on top of mobs by jumping on them", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        ridingTarget = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // If we're already riding, cancel
        if (mc.player.hasVehicle()) {
            ridingTarget = null;
            return;
        }

        // Find nearest mob to stand on
        LivingEntity nearest = null;
        double minDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof MobEntity)) continue;
            LivingEntity living = (LivingEntity) entity;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;
            if (dist < minDist) {
                minDist = dist;
                nearest = living;
            }
        }

        if (nearest != null) {
            // Teleport player to stand on top of the mob
            double topY = nearest.getY() + nearest.getHeight();
            Vec3d current = mc.player.getPos();
            double dy = topY - current.y;

            if (Math.abs(dy) < 0.5) {
                // Stand on mob by zeroing Y velocity and matching position
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(vel.x, Math.max(0, vel.y), vel.z);
                mc.player.setPosition(nearest.getX(), topY, nearest.getZ());
                mc.player.fallDistance = 0.0f;
            } else if (dy > 0) {
                // Jump up toward mob
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(vel.x, Math.min(dy, 0.42), vel.z);
            }
        }
    }
}
