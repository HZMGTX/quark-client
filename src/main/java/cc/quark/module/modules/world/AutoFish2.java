package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * AutoFish2 - Enhanced auto-fisher that detects bobber splash (Y-velocity drop)
 * and recasts after a configurable wait period.
 */
public class AutoFish2 extends Module {

    private final IntSetting waitMs = register(new IntSetting(
            "WaitMs", "Maximum wait time before recast (ms)", 3000, 500, 10000));
    private final IntSetting castDelay = register(new IntSetting(
            "CastDelay", "Delay between reel-in and recast (ms)", 500, 100, 3000));

    private final TimerUtil waitTimer  = new TimerUtil();
    private final TimerUtil castTimer  = new TimerUtil();

    private boolean waitingForBite = false;
    private boolean reeling        = false;
    private double  lastBobberY    = Double.MIN_VALUE;

    public AutoFish2() {
        super("AutoFish2", "Enhanced auto-fisher with wait detection", Category.WORLD);
    }

    @Override
    public void onEnable() {
        waitingForBite = false;
        reeling        = false;
        lastBobberY    = Double.MIN_VALUE;
        waitTimer.reset();
        castTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Must hold a fishing rod
        ItemStack held = mc.player.getMainHandStack();
        if (!(held.getItem() instanceof FishingRodItem)) return;

        FishingBobberEntity bobber = mc.player.fishHook;

        // Phase 1: No bobber out — cast
        if (bobber == null) {
            if (!reeling && castTimer.hasReached(castDelay.get())) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                waitingForBite = true;
                reeling        = false;
                waitTimer.reset();
                lastBobberY    = Double.MIN_VALUE;
            }
            return;
        }

        // Phase 2: Bobber is out — detect bite via downward Y jerk
        if (waitingForBite) {
            double bobberY = bobber.getY();
            if (lastBobberY != Double.MIN_VALUE) {
                double dy = bobberY - lastBobberY;
                boolean bitDetected = dy < -0.05; // significant downward dip
                boolean timeout     = waitTimer.hasReached(waitMs.get());

                if (bitDetected || timeout) {
                    // Reel in
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    waitingForBite = false;
                    reeling        = true;
                    castTimer.reset();
                }
            }
            lastBobberY = bobberY;
        }
    }
}
