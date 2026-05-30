package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.Vec3d;

/**
 * ArrowDodge — detects incoming arrows and strafes perpendicular to dodge them.
 */
public class ArrowDodge extends Module {

    private final DoubleSetting dodgeSpeed = register(new DoubleSetting(
            "DodgeSpeed", "Horizontal strafe speed when dodging", 0.3, 0.1, 1.0));

    private final DoubleSetting detectionRange = register(new DoubleSetting(
            "DetectionRange", "Radius to check for arrows", 10.0, 3.0, 20.0));

    public ArrowDodge() {
        super("ArrowDodge", "Automatically strafes to dodge incoming arrows", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        Vec3d playerPos = mc.player.getPos();
        ArrowEntity closestArrow = null;
        double closestThreat = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ArrowEntity arrow)) continue;

            Vec3d arrowPos = arrow.getPos();
            Vec3d arrowVel = arrow.getVelocity();

            double distToPlayer = arrowPos.distanceTo(playerPos);
            if (distToPlayer > detectionRange.get()) continue;

            // Check if the arrow is heading toward the player
            Vec3d arrowToPlayer = playerPos.subtract(arrowPos);
            double dot = arrowVel.normalize().dotProduct(arrowToPlayer.normalize());
            if (dot < 0.7) continue; // Not heading toward us

            // Estimate closest approach distance
            double closestApproach = estimateClosestApproach(arrowPos, arrowVel, playerPos);
            if (closestApproach < 1.5 && closestApproach < closestThreat) {
                closestThreat = closestApproach;
                closestArrow = arrow;
            }
        }

        if (closestArrow == null) return;

        // Strafe perpendicular to the arrow's velocity
        Vec3d arrowVel = closestArrow.getVelocity().normalize();
        // Perpendicular in XZ plane (rotate 90 degrees)
        Vec3d perpendicular = new Vec3d(-arrowVel.z, 0, arrowVel.x).normalize();

        double speed = dodgeSpeed.get();
        mc.player.addVelocity(perpendicular.x * speed, 0, perpendicular.z * speed);
    }

    private double estimateClosestApproach(Vec3d arrowPos, Vec3d arrowVel, Vec3d playerPos) {
        // Simulate arrow path for a few ticks and find minimum distance
        double minDist = Double.MAX_VALUE;
        double px = arrowPos.x;
        double py = arrowPos.y;
        double pz = arrowPos.z;
        double vx = arrowVel.x;
        double vy = arrowVel.y;
        double vz = arrowVel.z;

        for (int t = 0; t < 20; t++) {
            double dx = px - playerPos.x;
            double dy = py - playerPos.y;
            double dz = pz - playerPos.z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist < minDist) minDist = dist;

            px += vx;
            py += vy;
            pz += vz;
            vy -= 0.05; // gravity
            vx *= 0.99;
            vy *= 0.99;
            vz *= 0.99;
        }
        return minDist;
    }
}
