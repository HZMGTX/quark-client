package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

/**
 * HitSelect - attacks the target chosen by the selected priority mode.
 */
public class HitSelect extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 3.5, 1.0, 6.0));
    private final ModeSetting mode = register(new ModeSetting("Mode", "Target priority", "Closest", "Closest", "Health", "Farthest"));

    public HitSelect() {
        super("HitSelect", "Attacks the target chosen by priority mode", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        LivingEntity best = null;
        double bestScore = mode.is("Farthest") ? -1 : Double.MAX_VALUE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;
            double score;
            if (mode.is("Health")) score = living.getHealth();
            else score = dist;
            if (mode.is("Farthest")) {
                if (score > bestScore) { bestScore = score; best = living; }
            } else {
                if (score < bestScore) { bestScore = score; best = living; }
            }
        }
        if (best != null) {
            mc.interactionManager.attackEntity(mc.player, best);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
