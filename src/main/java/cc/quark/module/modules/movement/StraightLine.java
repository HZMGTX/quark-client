package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

/**
 * StraightLine - snap movement yaw to the nearest 45° multiple in EventPreMotion,
 * forcing perfectly straight or diagonal movement.
 */
public class StraightLine extends Module {

    private final BoolSetting strict = register(new BoolSetting(
            "Strict", "Only snap when yaw is within threshold of a 45° angle", true));
    private final DoubleSetting threshold = register(new DoubleSetting(
            "Threshold", "Degrees from 45° multiple before snapping", 10.0, 1.0, 45.0));

    public StraightLine() {
        super("StraightLine", "Snap movement yaw to nearest 45° for perfectly straight/diagonal movement", Category.MOVEMENT);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        float yaw     = event.getYaw();
        // Snap to nearest 45°
        float snapped = Math.round(yaw / 45.0f) * 45.0f;
        float diff    = Math.abs(yaw - snapped);

        if (!strict.isEnabled() || diff <= (float) threshold.get()) {
            event.setYaw(snapped);
        }
    }
}
