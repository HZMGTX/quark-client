package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * FightBot — automatically sprints toward the closest enemy and attacks when in range.
 */
public class FightBot extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 4.0, 1.0, 8.0));

    private final ModeSetting target = register(new ModeSetting(
            "Target", "Which entities to target",
            "Players", "Players", "Mobs", "All"));

    private final BoolSetting sprint = register(new BoolSetting(
            "Sprint", "Sprint toward the target", true));

    public FightBot() {
        super("FightBot", "Automatically chases and attacks the nearest enemy", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        LivingEntity nearestTarget = findTarget();
        if (nearestTarget == null) {
            mc.player.setSprinting(false);
            return;
        }

        double dist = mc.player.distanceTo(nearestTarget);

        // Move toward the target
        Vec3d targetPos = nearestTarget.getPos();
        Vec3d playerPos = mc.player.getPos();
        Vec3d direction = targetPos.subtract(playerPos).normalize();

        if (dist > range.get()) {
            // Sprint toward target
            if (sprint.isEnabled()) {
                mc.player.setSprinting(true);
            }
            // Look toward target
            double dx = direction.x;
            double dz = direction.z;
            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            mc.player.setYaw(yaw);
            mc.player.headYaw = yaw;
            // Add movement velocity
            mc.player.setVelocity(direction.x * 0.3, mc.player.getVelocity().y, direction.z * 0.3);
        } else {
            // In range — attack
            if (sprint.isEnabled()) {
                mc.player.setSprinting(true);
            }
            mc.interactionManager.attackEntity(mc.player, nearestTarget);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private LivingEntity findTarget() {
        List<LivingEntity> candidates = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isRemoved() || living.getHealth() <= 0) continue;

            boolean isPlayer = entity instanceof PlayerEntity;
            boolean isMob = entity instanceof MobEntity;

            switch (target.get()) {
                case "Players" -> { if (!isPlayer) continue; }
                case "Mobs"    -> { if (!isMob) continue; }
                case "All"     -> { if (!isPlayer && !isMob) continue; }
            }

            // Skip friends
            if (isPlayer && EntityUtil.isFriend(entity)) continue;

            candidates.add(living);
        }
        if (candidates.isEmpty()) return null;
        candidates.sort(Comparator.comparingDouble(e -> mc.player.distanceTo(e)));
        return candidates.get(0);
    }
}
