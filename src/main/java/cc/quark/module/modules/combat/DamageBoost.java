package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/**
 * DamageBoost - maximises attack damage per swing by:
 * 1. Waiting for full attack cooldown (crit window).
 * 2. Ensuring the player is falling for a critical hit bonus.
 * 3. Optionally sprinting for the knockback modifier.
 *
 * The module attacks the nearest in-range player/mob when all conditions are met.
 */
public class DamageBoost extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 3.5, 2.0, 6.0));

    private final BoolSetting requireCrit = register(new BoolSetting(
            "Require Crit", "Only attack when falling (guaranteed crit)", true));

    private final BoolSetting forceSprint = register(new BoolSetting(
            "Force Sprint", "Keep sprinting for +30% knockback", true));

    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "Only Players", "Only target other players", false));

    private final DoubleSetting cooldownPct = register(new DoubleSetting(
            "Cooldown %", "Minimum attack cooldown before striking", 100.0, 50.0, 100.0));

    private final BoolSetting swing = register(new BoolSetting(
            "Swing", "Play hand-swing animation", true));

    public DamageBoost() {
        super("DamageBoost", "Attacks with optimised crit and sprint timing for max damage", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Cooldown gate
        if (mc.player.getAttackCooldownProgress(0f) < (cooldownPct.get() / 100.0)) return;

        // Crit check: must be falling, not on ground, not in water, no blindness/levitation
        if (requireCrit.isEnabled()) {
            boolean canCrit = !mc.player.isOnGround()
                    && mc.player.getVelocity().y < 0
                    && !mc.player.isTouchingWater()
                    && !mc.player.isInLava()
                    && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                    && !mc.player.isRiding();
            if (!canCrit) return;
        }

        // Sprint
        if (forceSprint.isEnabled() && !mc.player.isSneaking()) {
            mc.player.setSprinting(true);
        }

        // Find nearest target
        LivingEntity target = null;
        double best = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof LivingEntity living)) continue;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;
            if (onlyPlayers.isEnabled() && !(e instanceof PlayerEntity)) continue;
            double d = mc.player.distanceTo(e);
            if (d <= range.get() && d < best) { best = d; target = living; }
        }
        if (target == null) return;

        mc.interactionManager.attackEntity(mc.player, target);
        if (swing.isEnabled()) mc.player.swingHand(Hand.MAIN_HAND);
    }
}
