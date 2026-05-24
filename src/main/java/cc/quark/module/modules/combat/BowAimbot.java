package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.EntityUtil;
import cc.quark.util.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * BowAimbot - auto-aims the bow at the nearest player, compensating for arrow drop
 * due to gravity so the shot lands on target.
 *
 * <p>Arrow physics (vanilla):
 * <ul>
 *   <li>Initial speed  = 3.0 blocks/tick at full charge (1 second draw).</li>
 *   <li>Gravity        = 0.05 blocks/tickÂ² downward per tick.</li>
 *   <li>Drag           = 0.99 multiplier per tick (air resistance).</li>
 * </ul>
 * We solve for the pitch angle that lands the arrow on the target's position by
 * binary-searching the launch angle space.
 */
public class BowAimbot extends Module {

    // Arrow physics constants (vanilla 1.20.4)
    private static final double ARROW_SPEED   = 3.0;   // blocks/tick at full charge
    private static final double GRAVITY       = 0.05;  // blocks/tickÂ²
    private static final double DRAG          = 0.99;  // multiplier per tick

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Maximum target distance in blocks", 60.0, 10.0, 100.0));

    private final BoolSetting predictMovement = register(new BoolSetting(
            "Predict Movement", "Lead the target based on current velocity", true));

    private final BoolSetting onlyOnRightClick = register(new BoolSetting(
            "Only On Right Click", "Only aim while right-click (charging bow) is held", true));

    public BowAimbot() {
        super("BowAimbot", "Auto-aims the bow with arrow drop compensation", Category.COMBAT);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null || mc.world == null) return;

        // Must be holding a bow
        var held = mc.player.getMainHandStack();
        if (!(held.getItem() instanceof BowItem)) return;

        // Only while right-click is held (bow charging)
        if (onlyOnRightClick.isEnabled() && !mc.options.useKey.isPressed()) return;

        // Find nearest player target
        PlayerEntity target = findNearestPlayer();
        if (target == null) return;

        Vec3d origin = mc.player.getEyePos();
        Vec3d targetPos = target.getEyePos();

        // Lead the shot if configured
        if (predictMovement.isEnabled()) {
            double dist = origin.distanceTo(targetPos);
            double flightTicks = estimateFlightTicks(dist);
            Vec3d vel = target.getVelocity();
            targetPos = targetPos.add(vel.multiply(flightTicks));
        }

        // Compute yaw
        float yaw = RotationUtil.getYaw(targetPos);

        // Compute compensated pitch via binary search on launch angle
        float pitch = computePitch(origin, targetPos);

        // Smooth rotation toward computed angles
        float newYaw   = RotationUtil.smoothYaw(event.getYaw(), yaw, 10f);
        float newPitch = RotationUtil.smoothPitch(event.getPitch(), pitch, 10f);

        event.setYaw(newYaw);
        event.setPitch(MathHelper.clamp(newPitch, -90f, 90f));
        mc.player.setYaw(newYaw);
        mc.player.setPitch(MathHelper.clamp(newPitch, -90f, 90f));
    }

    /**
     * Binary-searches for the launch pitch angle (in degrees) whose arc passes through
     * the target position, accounting for gravity and drag.
     */
    private float computePitch(Vec3d origin, Vec3d target) {
        double dx = target.x - origin.x;
        double dy = target.y - origin.y;
        double dz = target.z - origin.z;
        double horizDist = Math.sqrt(dx * dx + dz * dz);

        // Binary search: angles from -90 (straight up) to 90 (straight down)
        // We want the angle that minimises |simulated_y - target_y| at the point
        // the arrow covers the horizontal distance.
        float low  = -90f;
        float high = 90f;

        for (int i = 0; i < 100; i++) {
            float mid = (low + high) / 2f;
            double simY = simulateArrowY(Math.toRadians(mid), horizDist);
            if (simY > dy) {
                low = mid;
            } else {
                high = mid;
            }
        }

        return (low + high) / 2f;
    }

    /**
     * Simulates the Y displacement of an arrow launched at {@code pitchRad} radians
     * (negative = upward in Minecraft convention) when it has traveled {@code horizDist}
     * blocks horizontally.
     *
     * @param pitchRad  launch angle in radians (-Ï€/2 = straight up, Ï€/2 = straight down)
     * @param horizDist target horizontal distance
     * @return simulated Y displacement from origin
     */
    private double simulateArrowY(double pitchRad, double horizDist) {
        // Minecraft pitch convention: negative pitch = looking up
        double vy = -Math.sin(pitchRad) * ARROW_SPEED;
        double vxz = Math.cos(pitchRad) * ARROW_SPEED;

        double x = 0;
        double y = 0;
        double vxzCurrent = vxz;
        double vyCurrent  = vy;

        for (int t = 0; t < 200; t++) {
            x += vxzCurrent;
            y += vyCurrent;
            vyCurrent  = (vyCurrent - GRAVITY) * DRAG;
            vxzCurrent *= DRAG;

            if (x >= horizDist) {
                // Interpolate Y at exact horizDist
                double overshoot = x - horizDist;
                double fraction  = overshoot / (vxzCurrent / DRAG + 1e-9);
                return y - vyCurrent * fraction;
            }
        }
        return y;
    }

    /**
     * Rough estimate of flight ticks to cover a horizontal distance at full charge.
     */
    private double estimateFlightTicks(double dist) {
        double vxz = ARROW_SPEED;
        double x = 0;
        for (int t = 0; t < 200; t++) {
            x += vxz;
            vxz *= DRAG;
            if (x >= dist) return t;
        }
        return dist / (ARROW_SPEED * 0.9);
    }

    private PlayerEntity findNearestPlayer() {
        if (mc.world == null) return null;

        List<PlayerEntity> candidates = new ArrayList<>();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p)) continue;
            if (p == mc.player) continue;
            if (p.isDead() || p.getHealth() <= 0) continue;
            if (EntityUtil.distanceTo(p) > range.get()) continue;
            candidates.add(p);
        }

        candidates.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
        return candidates.isEmpty() ? null : candidates.get(0);
    }
}
