package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import java.util.Random;

public class HitChance extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range in blocks", 3.5, 1.0, 6.0));
    private final DoubleSetting chance = register(new DoubleSetting("Chance", "Percent chance to hit each opportunity", 85.0, 1.0, 100.0));
    private final BoolSetting humanize = register(new BoolSetting("Humanize", "Adds timing jitter to simulate human variation", true));

    private final Random random = new Random();
    private final TimerUtil jitterTimer = new TimerUtil();
    private long nextDelay = 0L;

    public HitChance() {
        super("HitChance", "Randomizes attacks to simulate human hit patterns", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        jitterTimer.reset();
        nextDelay = 0L;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0.0f) < 1.0f) return;

        if (humanize.isEnabled() && !jitterTimer.hasReached(nextDelay)) return;

        if (random.nextDouble() * 100.0 > chance.get()) {
            if (humanize.isEnabled()) {
                nextDelay = 50 + (long)(random.nextDouble() * 100.0);
                jitterTimer.reset();
            }
            return;
        }

        LivingEntity target = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;
            if (!(entity instanceof PlayerEntity)) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist > range.get() || dist >= bestDist) continue;
            bestDist = dist;
            target = living;
        }

        if (target == null) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);

        if (humanize.isEnabled()) {
            nextDelay = (long)(random.nextDouble() * 100.0);
            jitterTimer.reset();
        }
    }
}
