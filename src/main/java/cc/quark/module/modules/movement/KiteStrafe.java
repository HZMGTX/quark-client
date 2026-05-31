package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class KiteStrafe extends Module {

    private final DoubleSetting radius = register(new DoubleSetting(
            "Radius", "Combat distance to maintain from target", 4.0, 1.0, 10.0));

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Strafing angular speed (radians/tick)", 0.06, 0.01, 0.3));

    private double angle = 0.0;

    public KiteStrafe() {
        super("KiteStrafe", "Circles target while maintaining combat distance", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        angle = 0.0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        Entity target = findTarget();
        if (target == null) return;

        angle += speed.get();
        if (angle > Math.PI * 2) angle -= Math.PI * 2;

        double r = radius.get();
        double targetX = target.getX() + Math.cos(angle) * r;
        double targetZ = target.getZ() + Math.sin(angle) * r;

        double dx = targetX - mc.player.getX();
        double dz = targetZ - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        Vec3d vel = mc.player.getVelocity();
        if (dist > 0.15) {
            double moveSpeed = speed.get() * r * 0.8;
            mc.player.setVelocity((dx / dist) * moveSpeed, vel.y, (dz / dist) * moveSpeed);
            mc.player.setSprinting(true);
        }

        // Face toward target
        double lookDx = target.getX() - mc.player.getX();
        double lookDz = target.getZ() - mc.player.getZ();
        float yaw = (float) (Math.toDegrees(Math.atan2(lookDz, lookDx)) - 90);
        mc.player.setYaw(yaw);
    }

    private Entity findTarget() {
        if (mc.targetedEntity instanceof LivingEntity) return mc.targetedEntity;
        Entity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof LivingEntity)) continue;
            double d = mc.player.squaredDistanceTo(e);
            if (d < closestDist) {
                closestDist = d;
                closest = e;
            }
        }
        return closest;
    }
}
