package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class Orbit extends Module {

    private final DoubleSetting radius = register(new DoubleSetting(
            "Radius", "Orbit radius in blocks", 3.0, 1.0, 10.0));

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Orbit angular speed (radians per tick)", 0.05, 0.01, 0.3));

    private double angle = 0.0;

    public Orbit() {
        super("Orbit", "Circles around the nearest targeted entity", Category.MOVEMENT);
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

        Vec3d vel = mc.player.getVelocity();
        double dx = targetX - mc.player.getX();
        double dz = targetZ - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist > 0.1) {
            double moveSpeed = 0.3;
            mc.player.setVelocity((dx / dist) * moveSpeed, vel.y, (dz / dist) * moveSpeed);
        }

        // Face the target
        double lookDx = target.getX() - mc.player.getX();
        double lookDz = target.getZ() - mc.player.getZ();
        float yaw = (float) (Math.toDegrees(Math.atan2(lookDz, lookDx)) - 90);
        mc.player.setYaw(yaw);
    }

    private Entity findTarget() {
        if (mc.targetedEntity != null) return mc.targetedEntity;
        Entity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof net.minecraft.entity.LivingEntity)) continue;
            double d = mc.player.squaredDistanceTo(e);
            if (d < closestDist) {
                closestDist = d;
                closest = e;
            }
        }
        return closest;
    }
}
