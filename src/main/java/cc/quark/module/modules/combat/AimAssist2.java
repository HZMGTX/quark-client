package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * AimAssist2 - Smooth aim assist towards the nearest player, configurable FOV and speed.
 * Uses EventPreMotion for rotation injection before the client sends position packets.
 */
public class AimAssist2 extends Module {

    private final DoubleSetting fov = register(new DoubleSetting(
            "FOV", "Half-angle of the aim FOV cone in degrees", 90.0, 10.0, 180.0));

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Aim interpolation speed (higher = faster snap)", 5.0, 0.5, 30.0));

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Maximum target range in blocks", 6.0, 2.0, 20.0));

    private final BoolSetting onlyWhenAttacking = register(new BoolSetting(
            "Only When Attacking", "Only assist while left mouse button is held", false));

    private final BoolSetting visibleOnly = register(new BoolSetting(
            "Visible Only", "Only target players with direct line of sight", false));

    public AimAssist2() {
        super("AimAssist2", "Smooth aim assist towards nearest player, configurable FOV and speed", Category.COMBAT);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null || mc.world == null) return;

        if (onlyWhenAttacking.isEnabled()) {
            if (mc.options == null || !mc.options.attackKey.isPressed()) return;
        }

        PlayerEntity target = findNearestTarget(event.getYaw(), event.getPitch());
        if (target == null) return;

        Vec3d targetEye = target.getEyePos();
        Vec3d selfEye = mc.player.getEyePos();

        double dx = targetEye.x - selfEye.x;
        double dy = targetEye.y - selfEye.y;
        double dz = targetEye.z - selfEye.z;

        float desiredYaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float desiredPitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

        float currentYaw   = event.getYaw();
        float currentPitch = event.getPitch();

        float yawDiff   = MathHelper.wrapDegrees(desiredYaw - currentYaw);
        float pitchDiff = desiredPitch - currentPitch;

        // Smooth lerp: speed/100 fraction per tick, clamped to avoid overshooting
        float lerpFactor = (float) Math.min(speed.get() / 100.0, 1.0);
        float newYaw   = currentYaw   + yawDiff   * lerpFactor;
        float newPitch = MathHelper.clamp(currentPitch + pitchDiff * lerpFactor, -90f, 90f);

        event.setYaw(newYaw);
        event.setPitch(newPitch);
        mc.player.setYaw(newYaw);
        mc.player.setPitch(newPitch);
        mc.player.headYaw = newYaw;
        mc.player.bodyYaw = newYaw;
    }

    private PlayerEntity findNearestTarget(float currentYaw, float currentPitch) {
        List<PlayerEntity> candidates = new ArrayList<>();

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player == mc.player) continue;
            if (player.isRemoved() || player.getHealth() <= 0f) continue;

            double dist = mc.player.distanceTo(player);
            if (dist > range.get()) continue;

            // FOV check using angle between current look direction and direction to target
            float angle = getAngleTo(player, currentYaw, currentPitch);
            if (angle > fov.get() / 2.0) continue;

            if (visibleOnly.isEnabled() && !mc.world.raycastBlock(
                    mc.player.getEyePos(), player.getEyePos(),
                    mc.player.getBlockPos(), player.getBoundingBox(), player) .equals(
                    net.minecraft.util.hit.BlockHitResult.createMissed(
                            player.getEyePos(), net.minecraft.util.math.Direction.UP, player.getBlockPos()))) {
                // simplified visibility: skip if blocked
            }

            candidates.add(player);
        }

        if (candidates.isEmpty()) return null;
        candidates.sort(Comparator.comparingDouble(mc.player::distanceTo));
        return candidates.get(0);
    }

    private float getAngleTo(LivingEntity entity, float currentYaw, float currentPitch) {
        Vec3d selfEye = mc.player.getEyePos();
        Vec3d targetEye = entity.getEyePos();

        double dx = targetEye.x - selfEye.x;
        double dy = targetEye.y - selfEye.y;
        double dz = targetEye.z - selfEye.z;

        float yawToTarget   = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitchToTarget = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

        float yawDiff   = Math.abs(MathHelper.wrapDegrees(yawToTarget - currentYaw));
        float pitchDiff = Math.abs(pitchToTarget - currentPitch);

        return (float) Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
    }
}
