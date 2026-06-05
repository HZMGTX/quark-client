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
 * AuraDelay — attacks the closest in-range entity with a configurable delay
 * and optional ±20 % random jitter to better emulate human-like timing.
 */
public class AuraDelay extends Module {

    private final IntSetting  delay     = register(new IntSetting ("Delay",      "Base ms between attacks",          200, 50, 500));
    private final BoolSetting randomize = register(new BoolSetting("Randomize",  "Add ±20% random jitter to delay",  true));
    private final BoolSetting players   = register(new BoolSetting("Players",    "Target players",                   true));
    private final BoolSetting mobs      = register(new BoolSetting("Mobs",       "Target hostile mobs",              true));

    private static final double RANGE = 3.5;

    private final TimerUtil timer  = new TimerUtil();
    private final Random    random = new Random();

    public AuraDelay() {
        super("AuraDelay", "Attacks nearest target with a configurable delay and jitter", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @Override
    public String getSuffix() {
        return delay.get() + "ms";
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        double effectiveDelay = delay.get();
        if (randomize.isEnabled()) {
            double jitter = effectiveDelay * 0.20;
            effectiveDelay += (random.nextDouble() * 2 - 1) * jitter;
        }

        if (!timer.hasReached(effectiveDelay)) return;

        LivingEntity target = null;
        double bestDist = RANGE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;
            boolean isPlayer = entity instanceof PlayerEntity;
            if (isPlayer && !players.isEnabled()) continue;
            if (!isPlayer && !mobs.isEnabled()) continue;
            double d = mc.player.distanceTo(entity);
            if (d < bestDist) { bestDist = d; target = living; }
        }

        if (target != null) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            timer.reset();
        }
    }
}
