package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;

/**
 * Stab - quick single-target sword attack on the closest enemy.
 */
public class Stab extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 3.0, 1.0, 6.0));

    public Stab() {
        super("Stab", "Quick sword attack on the closest enemy", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem)) return;
        LivingEntity closest = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;
            if (dist < bestDist) { bestDist = dist; closest = living; }
        }
        if (closest != null) {
            mc.interactionManager.attackEntity(mc.player, closest);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
