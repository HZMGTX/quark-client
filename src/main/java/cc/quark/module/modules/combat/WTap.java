package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class WTap extends Module {

    private final BoolSetting reSprint = register(new BoolSetting(
            "Re-Sprint", "Automatically sprint again after the reset", true));

    private final BoolSetting sprintReset = register(new BoolSetting(
            "Sprint Reset", "Also call setSprinting(false) to reset sprint momentum", true));

    private int resetTicks = 0;
    private boolean wWasPressed = false;

    public WTap() {
        super("WTap", "Sprint-resets on every attack to maximise knockback", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null && resetTicks > 0) {
            mc.options.forwardKey.setPressed(false);
            resetTicks = 0;
        }
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;

        // Only w-tap when actually moving forward
        if (mc.player.input.movementForward <= 0) return;

        if (sprintReset.isEnabled()) {
            mc.player.setSprinting(false);
        }

        // Release W key to trigger sprint reset server-side
        wWasPressed = mc.player.input.movementForward > 0;
        mc.options.forwardKey.setPressed(false);
        resetTicks = 2;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (resetTicks > 0 && --resetTicks == 0) {
            // Restore W key state
            if (wWasPressed) {
                mc.options.forwardKey.setPressed(true);
            }
            if (reSprint.isEnabled()) {
                mc.player.setSprinting(true);
            }
        }
    }
}
