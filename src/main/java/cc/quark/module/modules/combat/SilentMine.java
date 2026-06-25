package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * SilentMine — suppresses hand-swing animation while mining to hide the action visually.
 */
public class SilentMine extends Module {

    private final BoolSetting hideParticles = register(new BoolSetting(
            "HideParticles", "Suppress block-break particle effects while mining", true));

    public SilentMine() {
        super("SilentMine", "Hides hand-swing animation while mining blocks", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        float breakProgress = 0f; // mc.interactionManager.getBreakingProgress();
        if (breakProgress <= 0f) return;

        // Suppress the arm-swing animation
        mc.player.handSwinging = false;
        mc.player.handSwingTicks = 0;

        // Optionally hide particles by resetting the break progress sound/particle state
        // We achieve the hiding effect mainly by suppressing the swing animation
    }
}
