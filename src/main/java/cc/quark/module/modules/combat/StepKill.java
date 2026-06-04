package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class StepKill extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 4.0, 1.0, 6.0));

    private final DoubleSetting stepHeight = register(new DoubleSetting(
            "Step Height", "Height difference to qualify as elevated", 1.0, 0.1, 3.0));

    public StepKill() {
        super("StepKill", "Steps up to elevated enemies to land hits", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!mc.player.isOnGround()) return;

        float cooldown = mc.player.getAttackCooldownProgress(0.0f);
        if (cooldown < 0.9f) return;

        LivingEntity bestTarget = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity)) continue;
            LivingEntity living = (LivingEntity) entity;
            if (living.isDead() || living.getHealth() <= 0f) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;

            // Target must be above player by stepHeight
            double heightDiff = entity.getY() - mc.player.getY();
            if (heightDiff < stepHeight.get()) continue;

            if (dist < bestDist) {
                bestDist = dist;
                bestTarget = living;
            }
        }

        if (bestTarget != null) {
            // Step up by briefly boosting Y velocity
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, 0.42 * stepHeight.get(), vel.z);

            // Attack after stepping
            mc.interactionManager.attackEntity(mc.player, bestTarget);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
