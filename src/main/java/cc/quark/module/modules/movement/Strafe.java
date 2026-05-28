package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class Strafe extends Module {

    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Strafing speed multiplier", 1.0, 0.1, 3.0));
    private final BoolSetting sprint = register(new BoolSetting("Sprint", "Force sprint while strafing", true));

    public Strafe() {
        super("Strafe", "Optimal strafing movement at ideal 45-degree angles", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;

        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        if (fwd == 0 && side == 0) return;

        double len = Math.sqrt(fwd * fwd + side * side);
        if (len == 0) return;

        float normFwd = (float) (fwd / len);
        float normSide = (float) (side / len);

        float yawRad = (float) Math.toRadians(mc.player.getYaw());
        double base = 0.26 * speed.get();

        double x = (-Math.sin(yawRad) * normFwd + Math.cos(yawRad) * normSide) * base;
        double z = (Math.cos(yawRad) * normFwd + Math.sin(yawRad) * normSide) * base;

        event.setX(x);
        event.setZ(z);

        if (sprint.isEnabled()) {
            mc.player.setSprinting(true);
        }
    }
}
