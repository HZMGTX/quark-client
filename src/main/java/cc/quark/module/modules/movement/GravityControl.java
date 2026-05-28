package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;

public class GravityControl extends Module {

    private final ModeSetting mode = register(new ModeSetting("Mode", "Gravity mode", "Reduce", "Reduce", "Zero", "Reverse", "Custom"));
    private final DoubleSetting gravity = register(new DoubleSetting("Gravity", "Gravity multiplier (0=float, 1=normal, 2=double)", 0.5, 0.0, 2.0));

    public GravityControl() {
        super("GravityControl", "Control gravity strength while airborne", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        double currentY = event.getY();

        switch (mode.get()) {
            case "Zero" -> event.setY(0.0);
            case "Reverse" -> event.setY(-currentY);
            case "Reduce" -> {
                if (currentY < 0) event.setY(currentY * 0.5);
            }
            case "Custom" -> event.setY(currentY * gravity.get());
        }
    }
}
