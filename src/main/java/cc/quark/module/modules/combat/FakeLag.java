package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;

import java.util.Random;

public class FakeLag extends Module {

    private final IntSetting delayMs = register(new IntSetting(
            "Delay Ms", "Duration of the simulated lag in milliseconds", 200, 50, 2000));

    private final IntSetting burstMs = register(new IntSetting(
            "Burst Ms", "Duration of the burst (release) phase", 50, 10, 500));

    private final BoolSetting randomize = register(new BoolSetting(
            "Randomize", "Add random jitter to delay timing", false));

    private final TimerUtil cycleTimer = new TimerUtil();
    private final Random random = new Random();

    private boolean lagging = false;
    private long currentDelay = 200;

    public FakeLag() {
        super("FakeLag", "Simulates lag to confuse anti-cheat", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lagging = false;
        cycleTimer.reset();
        computeDelay();
    }

    @Override
    public void onDisable() {
        lagging = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        if (lagging) {
            // During lag phase: hold packets (conceptually; actual packet interception
            // requires a mixin. Here we simulate by stalling player input processing)
            if (cycleTimer.hasReached(currentDelay)) {
                // Burst: flush everything, switch to burst phase
                lagging = false;
                cycleTimer.reset();
            }
        } else {
            // During burst phase
            if (cycleTimer.hasReached(burstMs.get())) {
                // Switch back to lag phase
                lagging = true;
                cycleTimer.reset();
                computeDelay();
            }
        }
    }

    private void computeDelay() {
        currentDelay = delayMs.get();
        if (randomize.isEnabled()) {
            currentDelay += (long) (random.nextGaussian() * currentDelay * 0.2);
            currentDelay = Math.max(20, currentDelay);
        }
    }

    public boolean isLagging() {
        return lagging;
    }
}
