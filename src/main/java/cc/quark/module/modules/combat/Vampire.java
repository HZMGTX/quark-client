package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;

/**
 * Vampire — heals the player proportionally to damage dealt.
 * Monitors the target's HP before and after each attack, computes the delta,
 * and calls heal(ratio * delta) on the player.
 * Also applies a short Regeneration burst for visual feedback.
 */
public class Vampire extends Module {

    private final DoubleSetting range     = register(new DoubleSetting("Range",      "Auto-attack range",             3.5, 1.0, 6.0));
    private final DoubleSetting healRatio = register(new DoubleSetting("Heal Ratio", "Fraction of damage returned as HP", 0.3, 0.05, 1.0));
    private final IntSetting    regenTicks= register(new IntSetting   ("Regen Ticks","Regen effect duration (ticks)",  20,  5,  100));

    /** HP snapshot before the attack — used to compute the delta */
    private float prevTargetHp = -1f;
    private LivingEntity lastTarget = null;

    public Vampire() {
        super("Vampire", "Heals player for a fraction of damage dealt on each hit", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        prevTargetHp = -1f;
        lastTarget = null;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (!(event.getTarget() instanceof LivingEntity living)) return;
        prevTargetHp = living.getHealth();
        lastTarget   = living;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Resolve pending heal from last attack
        if (lastTarget != null && prevTargetHp > 0f) {
            float hpAfter = lastTarget.getHealth();
            float delta   = prevTargetHp - hpAfter;
            if (delta > 0.01f) {
                float healAmount = (float) (delta * healRatio.get());
                mc.player.heal(healAmount);
                mc.player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.REGENERATION, regenTicks.get(), 0, false, false));
            }
            prevTargetHp = -1f;
            lastTarget   = null;
        }

        // Auto-attack nearest entity
        if (mc.player.getAttackCooldownProgress(0f) < 1.0f) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isRemoved()) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            prevTargetHp = living.getHealth();
            lastTarget   = living;
            mc.interactionManager.attackEntity(mc.player, living);
            mc.player.swingHand(Hand.MAIN_HAND);
            return;
        }
    }
}
