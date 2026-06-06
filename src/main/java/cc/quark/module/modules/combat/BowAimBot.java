package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * BowAimBot - Calculates bow projectile trajectory including gravity and adjusts
 * aim to hit moving targets. Accounts for target velocity for predictive aiming.
 */
public class BowAimBot extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Maximum target acquisition range (blocks)", 60.0, 8.0, 100.0));

    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "Only Players", "Only target other players", true));

    private final BoolSetting predictMovement = register(new BoolSetting(
            "Predict Movement", "Lead the aim to account for target movement", true));

    private final DoubleSetting minCharge = register(new DoubleSetting(
            "Min Charge", "Minimum bow charge (0-1) before aiming", 0.5, 0.0, 1.0));

    // Track previous target position for velocity calculation
    private Vec3d prevTargetPos = null;
    private LivingEntity prevTarget = null;

    public BowAimBot() {
        super("BowAimBot", "Calculates bow trajectory and adjusts aim to hit moving targets", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        prevTargetPos = null;
        prevTarget = null;
    }

    @Override
    public void onDisable() {
        prevTargetPos = null;
        prevTarget = null;
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null || mc.world == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof BowItem)) return;

        // Check charge level
        int useTime = mc.player.getItemUseTime();
        double charge = Math.min(useTime / 20.0, 1.0);
        if (charge < minCharge.get()) return;

        LivingEntity target = findNearestTarget();
        if (target == null) {
            prevTargetPos = null;
            prevTarget = null;
            return;
        }

        // Arrow initial velocity based on charge
        // Arrow velocity formula: charge^2 * 3 (approximate for vanilla bow)
        double arrowSpeed = charge * charge * 3.0;
        arrowSpeed = Math.max(arrowSpeed, 0.1);

        // Arrow gravity per tick (vanilla: 0.05 downward)
        double gravity = 0.05;

        Vec3d eyePos = mc.player.getEyePos();

        // Target body center position
        Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2.0, 0);

        // Predict target movement if enabled
        if (predictMovement.isEnabled() && prevTarget == target && prevTargetPos != null) {
            Vec3d velocity = targetPos.subtract(prevTargetPos);
            // Estimate flight time (rough: horizontal distance / arrowSpeed)
            double dx = targetPos.x - eyePos.x;
            double dz = targetPos.z - eyePos.z;
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);
            double flightTicks = horizontalDist / arrowSpeed;
            targetPos = targetPos.add(velocity.multiply(flightTicks));
        }

        prevTargetPos = target.getPos().add(0, target.getHeight() / 2.0, 0);
        prevTarget = target;

        double dx = targetPos.x - eyePos.x;
        double dy = targetPos.y - eyePos.y;
        double dz = targetPos.z - eyePos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        // Yaw: direct horizontal angle
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

        // Pitch: solve for the angle that compensates for gravity over flight time
        // Using simplified ballistic formula: pitch = -atan2(dy + g*(h/v)^2/2, h)
        // where h = horizontal distance, v = arrow speed
        double flightTime = horizontalDist / arrowSpeed;
        double gravityDrop = gravity * flightTime * flightTime * 0.5;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy + gravityDrop, horizontalDist));
        pitch = MathHelper.clamp(pitch, -90f, 90f);

        event.setYaw(yaw);
        event.setPitch(pitch);
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
        mc.player.headYaw = yaw;
        mc.player.bodyYaw = yaw;
    }

    private LivingEntity findNearestTarget() {
        LivingEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player) continue;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;
            if (onlyPlayers.isEnabled() && !(living instanceof PlayerEntity)) continue;

            double dist = mc.player.distanceTo(living);
            if (dist > range.get()) continue;
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = living;
            }
        }

        return nearest;
    }
}
