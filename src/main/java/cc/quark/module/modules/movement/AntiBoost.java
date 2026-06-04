package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class AntiBoost extends Module {

    private final DoubleSetting maxSpeed = register(new DoubleSetting(
            "Max Speed", "Maximum allowed horizontal speed per move event", 0.6, 0.1, 5.0));

    public AntiBoost() {
        super("AntiBoost", "Cancels unwanted velocity boosts from servers", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;

        double x = event.getX();
        double z = event.getZ();
        double horizontalSpeed = Math.sqrt(x * x + z * z);

        double limit = maxSpeed.get();

        if (horizontalSpeed > limit) {
            double scale = limit / horizontalSpeed;
            event.setX(x * scale);
            event.setZ(z * scale);
        }
    }
}
