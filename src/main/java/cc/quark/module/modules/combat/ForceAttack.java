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

public class ForceAttack extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting(
            "Threshold", "Attack cooldown threshold (0.0-1.0) before swinging", 0.95, 0.1, 1.0));

    public ForceAttack() {
        super("ForceAttack", "Attacks with max damage before cooldown reset", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        float cooldown = mc.player.getAttackCooldownProgress(0.0f);
        if (cooldown < threshold.get()) return;

        LivingEntity closest = null;
        double minDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;
            if (!(entity instanceof PlayerEntity)) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist < 4.0 && dist < minDist) {
                minDist = dist;
                closest = living;
            }
        }

        if (closest != null) {
            mc.interactionManager.attackEntity(mc.player, closest);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
