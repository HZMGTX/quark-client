package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/**
 * AttackRange - extends the effective melee attack range slightly by attacking
 * entities that are slightly beyond the vanilla 3-block reach.
 *
 * <p>The module finds the nearest enemy within the extended range and, when the
 * attack cooldown is ready, attacks them — effectively giving the player a
 * longer reach than normal without modifying any packets.
 */
public class AttackRange extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Extended attack reach in blocks (vanilla = 3.0)", 4.5, 3.0, 8.0));

    private final DoubleSetting cooldownThreshold = register(new DoubleSetting(
            "Cooldown %", "Attack cooldown percentage required before attacking", 90.0, 10.0, 100.0));

    private final BoolSetting swing = register(new BoolSetting(
            "Swing", "Play the hand-swing animation on attack", true));

    private final BoolSetting throughWalls = register(new BoolSetting(
            "Through Walls", "Attack entities even if blocks are in between", false));

    public AttackRange() {
        super("AttackRange", "Extends melee attack range slightly beyond vanilla", Category.COMBAT);
    }

    @Override
    public String getSuffix() {
        return String.format("%.1f", range.get());
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Check attack cooldown
        if (mc.player.getAttackCooldownProgress(0.0f) < (cooldownThreshold.get() / 100.0)) return;

        LivingEntity target = findNearestTarget();
        if (target == null) return;

        mc.interactionManager.attackEntity(mc.player, target);
        if (swing.isEnabled()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private LivingEntity findNearestTarget() {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        double r = range.get();

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity p)) continue;
            if (p.isDead() || p.getHealth() <= 0f) continue;

            double d = mc.player.getEyePos().distanceTo(e.getEyePos());
            if (d > r || d >= bestDist) continue;

            if (!throughWalls.isEnabled() && !mc.player.canSee(e)) continue;

            bestDist = d;
            best = p;
        }
        return best;
    }
}
