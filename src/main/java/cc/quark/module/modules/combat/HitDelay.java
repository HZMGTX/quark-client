package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import java.util.Random;

/**
 * HitDelay - introduces a randomized delay between melee hits to make the
 * attack pattern look more natural and bypass anti-cheat systems that flag
 * perfectly consistent click rates.
 */
public class HitDelay extends Module {

    private final IntSetting minDelay = register(new IntSetting(
            "Min Delay", "Minimum delay between hits (ms)", 80, 20, 500));

    private final IntSetting maxDelay = register(new IntSetting(
            "Max Delay", "Maximum delay between hits (ms)", 160, 50, 1000));

    private final IntSetting range = register(new IntSetting(
            "Range", "Attack range in blocks", 3, 2, 6));

    private final BoolSetting onlyFullCooldown = register(new BoolSetting(
            "Full Cooldown", "Only attack when attack cooldown is 100%", true));

    private final TimerUtil hitTimer = new TimerUtil();
    private final Random random = new Random();
    private int nextDelay = 100;

    public HitDelay() {
        super("HitDelay", "Adds randomized delay between hits to appear more human", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        nextDelay = randomDelay();
        hitTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!hitTimer.hasReached(nextDelay)) return;

        if (onlyFullCooldown.isEnabled()) {
            if (mc.player.getAttackCooldownProgress(0.0f) < 1.0f) return;
        }

        LivingEntity target = findNearestTarget();
        if (target == null) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);

        nextDelay = randomDelay();
        hitTimer.reset();
    }

    private int randomDelay() {
        int min = minDelay.get();
        int max = maxDelay.get();
        if (max <= min) return min;
        return min + random.nextInt(max - min);
    }

    private LivingEntity findNearestTarget() {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity p)) continue;
            if (p.isDead() || p.getHealth() <= 0f) continue;
            double d = mc.player.distanceTo(p);
            if (d <= range.get() && d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }
}
