package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/**
 * HitSelect — on EventAttack, cancels the attack if the target doesn't match
 * the configured Filter; also finds and attacks the highest-priority target
 * each tick.
 * Filter: All | Players | Mobs | Animals
 */
public class HitSelect extends Module {

    private final DoubleSetting range  = register(new DoubleSetting("Range",  "Attack range",   3.5, 1.0, 6.0));
    private final ModeSetting   filter = register(new ModeSetting  ("Filter", "Target type filter", "Players", "All", "Players", "Mobs", "Animals"));

    public HitSelect() {
        super("HitSelect", "Filters which entity types can be attacked", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (!matchesFilter(event.getTarget())) {
            event.cancel();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0f) < 1.0f) return;

        LivingEntity best = null;
        double bestDist = range.get();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;
            if (!matchesFilter(entity)) continue;
            double d = mc.player.distanceTo(entity);
            if (d < bestDist) { bestDist = d; best = living; }
        }

        if (best != null) {
            mc.interactionManager.attackEntity(mc.player, best);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private boolean matchesFilter(Entity entity) {
        if (entity == null) return false;
        return switch (filter.get()) {
            case "Players" -> entity instanceof PlayerEntity;
            case "Mobs"    -> entity instanceof MobEntity;
            case "Animals" -> entity instanceof AnimalEntity;
            default        -> entity instanceof LivingEntity; // All
        };
    }

    @Override
    public String getSuffix() {
        return filter.get();
    }
}
