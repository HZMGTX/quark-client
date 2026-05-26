package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

/**
 * SnapStrafe - scales movement velocity uniformly through the move event.
 */
public class SnapStrafe extends Module {

    private final DoubleSetting factor = register(new DoubleSetting(
            "Factor", "Movement scale", 1.4, 1.0, 3.0));

    public SnapStrafe() {
        super("SnapStrafe", "Scales strafe velocity", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;
        event.setX(mc.player.getVelocity().x * factor.get());
        event.setZ(mc.player.getVelocity().z * factor.get());
    }
}
