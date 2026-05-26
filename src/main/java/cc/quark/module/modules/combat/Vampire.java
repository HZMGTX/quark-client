package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;

/**
 * Vampire - applies regeneration to the player after landing a hit.
 */
public class Vampire extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 3.5, 1.0, 6.0));

    public Vampire() {
        super("Vampire", "Regenerates health after landing hits", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            mc.interactionManager.attackEntity(mc.player, living);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20, 0, false, false));
            return;
        }
    }
}
