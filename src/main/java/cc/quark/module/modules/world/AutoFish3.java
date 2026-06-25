package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * AutoFish3 - Advanced fishing automation with auto-cast, auto-reel,
 * bite detection, rod durability guard, and configurable sensitivity.
 */
public class AutoFish3 extends Module {

    private final IntSetting castDelay = register(new IntSetting(
            "CastDelay", "Delay between reel-in and recast (ms)", 400, 100, 3000));
    private final IntSetting maxWait = register(new IntSetting(
            "MaxWait", "Max time to wait for a bite before recasting (ms)", 15000, 2000, 60000));
    private final IntSetting sensitivity = register(new IntSetting(
            "Sensitivity", "Bite detection sensitivity (1=high, 10=low)", 3, 1, 10));
    private final BoolSetting autoSwitch = register(new BoolSetting(
            "AutoSwitch", "Auto-switch to fishing rod if one exists in hotbar", true));
    private final BoolSetting durabilityGuard = register(new BoolSetting(
            "DurabilityGuard", "Stop fishing if rod durability is critically low", true));

    private final TimerUtil castTimer = new TimerUtil();
    private final TimerUtil waitTimer = new TimerUtil();

    private boolean waitingForBite = false;
    private boolean reeling = false;
    private double lastBobberY = Double.MIN_VALUE;
    private int stableCount = 0;

    public AutoFish3() {
        super("AutoFish3", "Advanced fishing automation with auto-cast and auto-reel", Category.WORLD);
    }

    @Override
    public void onEnable() {
        waitingForBite = false;
        reeling = false;
        lastBobberY = Double.MIN_VALUE;
        stableCount = 0;
        castTimer.reset();
        waitTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Ensure we hold a fishing rod
        if (!holdingRod()) {
            if (autoSwitch.isEnabled()) {
                int rodSlot = findRodSlot();
                if (rodSlot != -1) mc.player.getInventory().selectedSlot = rodSlot;
                else return;
            } else {
                return;
            }
        }

        // Durability guard
        if (durabilityGuard.isEnabled()) {
            ItemStack rod = mc.player.getMainHandStack();
            int maxDmg = rod.getMaxDamage();
            if (maxDmg > 0 && rod.getDamage() >= maxDmg - 5) return;
        }

        FishingBobberEntity bobber = mc.player.fishHook;

        // No bobber — cast after delay
        if (bobber == null) {
            if (!reeling && castTimer.hasReached(castDelay.get())) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                waitingForBite = true;
                reeling = false;
                lastBobberY = Double.MIN_VALUE;
                stableCount = 0;
                waitTimer.reset();
            }
            return;
        }

        // Recast if we've waited too long
        if (waitingForBite && waitTimer.hasReached(maxWait.get())) {
            reel();
            return;
        }

        // Bite detection: bobber dips significantly downward
        if (waitingForBite) {
            double bobberY = bobber.getY();
            if (lastBobberY != Double.MIN_VALUE) {
                double dy = bobberY - lastBobberY;
                // Threshold scales with sensitivity: higher sensitivity = smaller dip triggers
                double threshold = -0.02 * (11 - sensitivity.get());
                if (dy < threshold) {
                    stableCount++;
                    if (stableCount >= 2) {
                        reel();
                        return;
                    }
                } else {
                    stableCount = 0;
                }
            }
            lastBobberY = bobberY;
        }
    }

    private void reel() {
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        waitingForBite = false;
        reeling = true;
        stableCount = 0;
        lastBobberY = Double.MIN_VALUE;
        castTimer.reset();
    }

    private boolean holdingRod() {
        if (mc.player == null) return false;
        return mc.player.getMainHandStack().getItem() instanceof FishingRodItem;
    }

    private int findRodSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof FishingRodItem) return i;
        }
        return -1;
    }
}
