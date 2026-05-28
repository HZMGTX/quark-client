package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.concurrent.ThreadLocalRandom;

public class TriggerBot extends Module {

    private final IntSetting minCps = register(new IntSetting(
            "Min CPS", "Minimum clicks per second", 8, 1, 20));

    private final IntSetting maxCps = register(new IntSetting(
            "Max CPS", "Maximum clicks per second", 12, 1, 20));

    private final BoolSetting onlySword = register(new BoolSetting(
            "Only Sword", "Only trigger when holding a sword or axe", true));

    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "Only Players", "Only trigger when aiming at other players", true));

    private final BoolSetting mobs = register(new BoolSetting(
            "Mobs", "Also trigger when aiming at hostile mobs", false));

    private final TimerUtil timer = new TimerUtil();
    private long effectiveDelay = 0L;

    public TriggerBot() {
        super("TriggerBot", "Automatically attacks when crosshair is on a valid target", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        effectiveDelay = computeEffectiveDelay();
    }

    @EventHandler
    public void onTick(EventTick event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Sword/axe filter
        if (onlySword.isEnabled()) {
            var held = mc.player.getMainHandStack().getItem();
            if (!(held instanceof SwordItem) && !(held instanceof AxeItem)) return;
        }

        // Get entity under crosshair
        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) return;

        EntityHitResult entityHit = (EntityHitResult) hit;
        Entity target = entityHit.getEntity();

        if (!(target instanceof LivingEntity living)) return;
        if (living.isDead() || living.getHealth() <= 0f) return;
        if (target == mc.player) return;

        boolean isPlayer = target instanceof PlayerEntity;
        boolean isMob    = target instanceof MobEntity && !isPlayer;

        // Entity type filter
        if (onlyPlayers.isEnabled() && !isPlayer) return;
        if (!onlyPlayers.isEnabled() && isMob && !mobs.isEnabled()) return;

        // 1.9 attack cooldown check
        if (mc.player.getAttackCooldownProgress(0.0f) < 0.9f) return;

        // CPS throttle using TimerUtil
        if (!timer.hasReached(effectiveDelay)) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);

        timer.reset();
        effectiveDelay = computeEffectiveDelay();
    }

    /**
     * Compute a random delay in milliseconds based on a random CPS between minCps and maxCps.
     */
    private long computeEffectiveDelay() {
        int min = Math.min(minCps.get(), maxCps.get());
        int max = Math.max(minCps.get(), maxCps.get());
        int randomCps = min == max ? min : ThreadLocalRandom.current().nextInt(min, max + 1);
        long baseDelay = (long)(1000.0 / Math.max(1, randomCps));
        // Apply ±10ms jitter for humanization
        long jitter = (long)(ThreadLocalRandom.current().nextGaussian() * 5.0);
        jitter = Math.max(-15L, Math.min(15L, jitter));
        return Math.max(10L, baseDelay + jitter);
    }
}
