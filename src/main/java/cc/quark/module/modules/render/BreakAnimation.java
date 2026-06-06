package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;

/**
 * BreakAnimation - Customizes the block breaking animation behavior.
 *
 * Uses a static instance pattern so mixins on BlockBreakingProgressManager
 * (or equivalent) can read the active settings without a module registry lookup.
 *
 * When "Disable" mode is chosen the breaking progress texture is simply not
 * rendered; when "Speed" is chosen the animation plays faster; when "Loop"
 * is chosen the stages cycle continuously for a pulsing effect.
 */
public class BreakAnimation extends Module {

    private static BreakAnimation instance;

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Block-break animation behavior",
            "Disable", "Disable", "Speed", "Loop"));

    private final DoubleSetting speedMult = register(new DoubleSetting(
            "Speed Multiplier", "Stage advancement multiplier (Speed mode)", 2.0, 1.0, 10.0));

    private final BoolSetting hideParticles = register(new BoolSetting(
            "Hide Particles", "Suppress block-break particles", false));

    public BreakAnimation() {
        super("BreakAnimation", "Customizes block breaking animation stages", Category.RENDER);
        instance = this;
    }

    public static BreakAnimation getInstance() { return instance; }

    /** Returns true when the breaking overlay should be suppressed entirely. */
    public static boolean shouldDisable() {
        return instance != null && instance.isEnabled() && instance.mode.is("Disable");
    }

    /** Returns the speed multiplier applied to break-progress ticks. */
    public static double getSpeedMult() {
        if (instance == null || !instance.isEnabled() || !instance.mode.is("Speed")) return 1.0;
        return instance.speedMult.get();
    }

    /** Returns true when break stages should loop continuously. */
    public static boolean isLoop() {
        return instance != null && instance.isEnabled() && instance.mode.is("Loop");
    }

    public static boolean shouldHideParticles() {
        return instance != null && instance.isEnabled() && instance.hideParticles.isEnabled();
    }

    @EventHandler
    public void onTick(EventTick e) {
        // Settings are read statically by mixins; no per-tick work needed here.
    }
}
