package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/**
 * StabAura - a rapid close-range melee aura that attacks multiple times per
 * second, optimized for point-blank stabbing combat.
 *
 * <p>Unlike KillAura (which focuses on full-cooldown hits), StabAura fires at
 * a configurable rate to maximise DPS in close quarters, trading per-hit damage
 * for total throughput.
 */
public class StabAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Stab attack range in blocks", 2.5, 1.5, 5.0));

    private final IntSetting attacksPerSecond = register(new IntSetting(
            "APS", "Target attacks per second", 8, 1, 20));

    private final BoolSetting sprint = register(new BoolSetting(
            "Sprint", "Keep sprinting while attacking", true));

    private final BoolSetting swing = register(new BoolSetting(
            "Swing", "Play swing animation", true));

    private final BoolSetting requireTarget = register(new BoolSetting(
            "Require Target", "Only attack when looking near the target", false));

    private final TimerUtil attackTimer = new TimerUtil();

    public StabAura() {
        super("StabAura", "Rapid close-range melee attack aura", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        attackTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        int aps = attacksPerSecond.get();
        long delayMs = 1000L / aps;

        if (!attackTimer.hasReached(delayMs)) return;

        LivingEntity target = findNearestTarget();
        if (target == null) return;

        if (requireTarget.isEnabled()) {
            // Check if crosshair is near the target
            if (mc.targetedEntity != target) return;
        }

        if (sprint.isEnabled()) mc.player.setSprinting(true);

        mc.interactionManager.attackEntity(mc.player, target);
        if (swing.isEnabled()) mc.player.swingHand(Hand.MAIN_HAND);

        attackTimer.reset();
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
            if (d <= r && d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }
}
