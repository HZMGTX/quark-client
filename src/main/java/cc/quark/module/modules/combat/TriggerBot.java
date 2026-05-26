package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.concurrent.ThreadLocalRandom;

public class TriggerBot extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Base delay in milliseconds between attacks", 50, 0, 500));

    private final DoubleSetting cps = register(new DoubleSetting(
            "CPS", "Maximum clicks per second (overrides Delay if lower)", 10.0, 1.0, 20.0));

    private final BoolSetting onlySword = register(new BoolSetting(
            "Only Sword", "Only trigger when holding a sword or axe", true));

    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "Only Players", "Only trigger when aiming at other players", true));

    private long lastClick = 0L;
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

        if (onlySword.isEnabled()) {
            var held = mc.player.getMainHandStack().getItem();
            if (!(held instanceof SwordItem) && !(held instanceof AxeItem)) return;
        }

        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) return;

        EntityHitResult entityHit = (EntityHitResult) hit;
        Entity target = entityHit.getEntity();

        if (!(target instanceof LivingEntity living)) return;
        if (living.isDead() || living.getHealth() <= 0f) return;

        if (onlyPlayers.isEnabled() && !(target instanceof PlayerEntity)) return;

        if (target == mc.player) return;

        // Check 1.9 attack cooldown
        if (mc.player.getAttackCooldownProgress(0.0f) < 0.9f) return;

        long now = System.currentTimeMillis();
        if (now - lastClick < effectiveDelay) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);

        lastClick = now;
        effectiveDelay = computeEffectiveDelay();
    }

    private long computeEffectiveDelay() {
        // Use the larger of: configured delay OR CPS-derived delay
        long baseDelay = delay.get();
        long cpsDelay = (long) (1000.0 / cps.get());
        long maxDelay = Math.max(baseDelay, cpsDelay);

        // Apply ±20ms gaussian jitter for humanization
        double jitter = ThreadLocalRandom.current().nextGaussian() * 10.0;
        jitter = Math.max(-20.0, Math.min(20.0, jitter));
        return Math.max(0L, maxDelay + (long) jitter);
    }
}
