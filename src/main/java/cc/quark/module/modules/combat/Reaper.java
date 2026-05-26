package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

/**
 * Reaper - finishes off low-health targets within range first.
 */
public class Reaper extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 4.0, 1.0, 6.0));
    private final IntSetting health = register(new IntSetting("Max Health", "Only target below this health", 8, 1, 20));

    public Reaper() {
        super("Reaper", "Finishes off low-health targets", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        LivingEntity best = null;
        float bestHealth = Float.MAX_VALUE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            if (living.getHealth() > health.get()) continue;
            if (living.getHealth() < bestHealth) { bestHealth = living.getHealth(); best = living; }
        }
        if (best != null) {
            mc.interactionManager.attackEntity(mc.player, best);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
