package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

/**
 * PowerHit — applies a Strength potion effect (Level 1-3) to the player
 * on each EventAttack, boosting outgoing melee damage.
 * Also acts as an auto-attacker when no manual attack fires.
 */
public class PowerHit extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range",  "Auto-attack range",         3.5, 1.0, 6.0));
    private final IntSetting    level = register(new IntSetting   ("Level",  "Strength effect level (1-3)", 1,   1,   3));

    public PowerHit() {
        super("PowerHit", "Applies Strength effect on each attack for boosted damage", Category.COMBAT);
    }

    @Override
    public String getSuffix() {
        return "Lvl " + level.get();
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        applyStrength();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0f) < 1.0f) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            applyStrength();
            mc.interactionManager.attackEntity(mc.player, living);
            mc.player.swingHand(Hand.MAIN_HAND);
            return;
        }
    }

    private void applyStrength() {
        if (mc.player == null) return;
        int amplifier = Math.max(0, level.get() - 1);
        mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, amplifier, false, false));
    }
}
