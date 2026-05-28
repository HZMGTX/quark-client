package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class StraightLine extends Module {

    private final BoolSetting strict = register(new BoolSetting("Strict", "Snap yaw within tight threshold (within 5 degrees)", true));

    public StraightLine() {
        super("StraightLine", "Force perfectly straight movement by snapping yaw to nearest 90 degrees", Category.MOVEMENT);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;

        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        if (fwd == 0 && side == 0) return;

        float yaw = event.getYaw();
        float snapped = Math.round(yaw / 90.0f) * 90.0f;
        float diff = Math.abs(yaw - snapped);

        if (strict.isEnabled()) {
            if (diff <= 5.0f) {
                event.setYaw(snapped);
            }
        } else {
            if (diff <= 45.0f) {
                event.setYaw(snapped);
            }
        }
    }
}
