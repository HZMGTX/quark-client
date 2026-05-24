package com.ghostclient.module.modules.movement;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.DoubleSetting;

/**
 * Step - allows the player to step up blocks without jumping by raising
 * {@code stepHeight} on the player entity.
 *
 * <p>smoothStep: gradually increases stepHeight over 3 ticks when the player
 * walks into a block, then lowers it back to normal, producing a smoother
 * animation that is less obvious to anti-cheat systems.
 */
public class Step extends Module {

    /** Vanilla default step height. */
    private static final float VANILLA_STEP = 0.6f;

    private final DoubleSetting height = register(new DoubleSetting(
            "Height", "Maximum block height to step up without jumping (blocks)", 1.0, 1.0, 2.5));

    private final BoolSetting onlyUpward = register(new BoolSetting(
            "Only Upward", "Only increase step height, never go below vanilla", true));

    private final BoolSetting smoothStep = register(new BoolSetting(
            "Smooth Step", "Gradually raise then lower step height over 3 ticks for natural look", false));

    // Smooth-step animation state
    private int   smoothPhase    = 0;   // 0=idle, 1=rising, 2=peak, 3=lowering
    private float smoothCurrent  = VANILLA_STEP;
    private int   phaseTicks     = 0;

    public Step() {
        super("Step", "Steps up blocks automatically without jumping", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        smoothPhase   = 0;
        smoothCurrent = VANILLA_STEP;
        phaseTicks    = 0;
        if (mc.player != null) applyStepHeight();
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.stepHeight = VANILLA_STEP;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (smoothStep.isEnabled()) {
            tickSmoothStep();
        } else {
            applyStepHeight();
        }
    }

    // -------------------------------------------------------------------------
    // Core logic
    // -------------------------------------------------------------------------

    private void applyStepHeight() {
        float desired = (float) height.get();
        if (onlyUpward.isEnabled()) {
            mc.player.stepHeight = Math.max(VANILLA_STEP, desired);
        } else {
            mc.player.stepHeight = desired;
        }
    }

    /**
     * Three-tick smooth step animation:
     * Tick 1: raise to half-way
     * Tick 2: raise to full target height
     * Tick 3: lower back to vanilla (step completed)
     */
    private void tickSmoothStep() {
        float target = (float) height.get();

        // Detect whether the player is pushing against a wall (likely about to step)
        boolean aboutToStep = mc.player.horizontalCollision && mc.player.isOnGround();

        if (aboutToStep && smoothPhase == 0) {
            smoothPhase   = 1;
            phaseTicks    = 0;
        }

        switch (smoothPhase) {
            case 1 -> {
                smoothCurrent = VANILLA_STEP + (target - VANILLA_STEP) * 0.5f;
                mc.player.stepHeight = smoothCurrent;
                smoothPhase  = 2;
            }
            case 2 -> {
                smoothCurrent = target;
                mc.player.stepHeight = smoothCurrent;
                smoothPhase  = 3;
            }
            case 3 -> {
                mc.player.stepHeight = VANILLA_STEP;
                smoothCurrent = VANILLA_STEP;
                smoothPhase  = 0;
            }
            default -> {
                // idle — keep at vanilla or set base step height
                mc.player.stepHeight = onlyUpward.isEnabled()
                        ? Math.max(VANILLA_STEP, target)
                        : VANILLA_STEP;
            }
        }
    }
}
