package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.EntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

/**
 * TargetFollow — maintains an optimal fighting distance from the nearest target,
 * sprinting toward them or backing away as needed.
 */
public class TargetFollow extends Module {

    private final DoubleSetting optimalDistance = register(new DoubleSetting(
            "OptimalDistance", "Ideal distance in blocks to maintain from target", 2.5, 1.0, 6.0));

    private final DoubleSetting minDistance = register(new DoubleSetting(
            "MinDistance", "Minimum distance before backing away from target", 1.5, 0.5, 3.0));

    private final DoubleSetting searchRange = register(new DoubleSetting(
            "SearchRange", "Range to look for targets", 20.0, 5.0, 50.0));

    public TargetFollow() {
        super("TargetFollow", "Maintains optimal combat distance from the nearest enemy", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        LivingEntity target = findTarget();
        if (target == null) {
            mc.player.setSprinting(false);
            return;
        }

        double dist = mc.player.distanceTo(target);
        Vec3d playerPos = mc.player.getPos();
        Vec3d targetPos = target.getPos();
        Vec3d direction = targetPos.subtract(playerPos).normalize();

        if (dist > optimalDistance.get()) {
            // Sprint toward target
            mc.player.setSprinting(true);
            mc.player.setVelocity(
                    direction.x * 0.3,
                    mc.player.getVelocity().y,
                    direction.z * 0.3);

            // Face the target
            faceTarget(target);
        } else if (dist < minDistance.get()) {
            // Back away
            mc.player.setSprinting(false);
            mc.player.setVelocity(
                    -direction.x * 0.2,
                    mc.player.getVelocity().y,
                    -direction.z * 0.2);
        } else {
            // In optimal range — just face the target
            mc.player.setSprinting(false);
            faceTarget(target);
        }
    }

    private LivingEntity findTarget() {
        List<LivingEntity> candidates = EntityUtil.getEntitiesOfType(LivingEntity.class, searchRange.get());
        candidates.removeIf(e -> !(e instanceof PlayerEntity));
        candidates.removeIf(EntityUtil::isFriend);
        if (candidates.isEmpty()) return null;
        candidates.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
        return candidates.get(0);
    }

    private void faceTarget(LivingEntity target) {
        if (mc.player == null) return;
        Vec3d eyes = mc.player.getEyePos();
        Vec3d targetEyes = target.getEyePos();

        double dx = targetEyes.x - eyes.x;
        double dy = targetEyes.y - eyes.y;
        double dz = targetEyes.z - eyes.z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
        mc.player.headYaw = yaw;
    }
}
