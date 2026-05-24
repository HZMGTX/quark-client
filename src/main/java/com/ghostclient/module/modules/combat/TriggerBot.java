package com.ghostclient.module.modules.combat;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.IntSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.concurrent.ThreadLocalRandom;

/**
 * TriggerBot - automatically attacks an entity when the player's crosshair
 * is directly on it, with a configurable and humanized delay between attacks.
 *
 * <p>Unlike KillAura, TriggerBot performs no rotation; it only fires when the
 * target is already naturally in the crosshair, making it much harder to detect.
 *
 * <p>Humanization: a random ±20 ms jitter is applied to the configured delay
 * to prevent perfectly regular attack patterns.
 */
public class TriggerBot extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Base delay in milliseconds between attacks", 50, 0, 200));

    private final BoolSetting onlySword = register(new BoolSetting(
            "Only Sword", "Only trigger when holding a sword or axe", true));

    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "Only Players", "Only trigger when aiming at other players", true));

    /** Timestamp (ms) of the last click we performed. */
    private long lastClick = 0L;

    /** Effective delay for the current attack (base ± jitter). */
    private long effectiveDelay = 0L;

    public TriggerBot() {
        super("TriggerBot", "Automatically attacks when crosshair is on a valid target", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastClick = 0L;
        effectiveDelay = computeEffectiveDelay();
    }

    @EventHandler
    public void onTick(EventTick event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Optionally restrict to sword / axe
        if (onlySword.isEnabled()) {
            var held = mc.player.getMainHandStack().getItem();
            if (!(held instanceof SwordItem) && !(held instanceof AxeItem)) return;
        }

        // Check what the crosshair is pointing at
        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) return;

        EntityHitResult entityHit = (EntityHitResult) hit;
        Entity target = entityHit.getEntity();

        // Must be a living entity
        if (!(target instanceof LivingEntity living)) return;
        if (living.isDead() || living.getHealth() <= 0f) return;

        // Player-only filter
        if (onlyPlayers.isEnabled() && !(target instanceof PlayerEntity)) return;

        // Skip ourselves
        if (target == mc.player) return;

        // Timing check
        long now = System.currentTimeMillis();
        if (now - lastClick < effectiveDelay) return;

        // Perform the attack
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingMainHand();

        lastClick = now;
        // Recompute jittered delay for next attack
        effectiveDelay = computeEffectiveDelay();
    }

    /**
     * Computes a humanized effective delay: {@code base ± random jitter (up to 20 ms)}.
     * Uses a Gaussian distribution centred on 0 with σ ≈ 10 ms, clamped to [0, base+20].
     */
    private long computeEffectiveDelay() {
        long base = delay.get();
        // Gaussian jitter: mean 0, std deviation ~10 ms, clamped to ±20 ms
        double jitter = ThreadLocalRandom.current().nextGaussian() * 10.0;
        jitter = Math.max(-20.0, Math.min(20.0, jitter));
        return Math.max(0L, base + (long) jitter);
    }
}
