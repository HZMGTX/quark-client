package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class Momentum extends Module {

    private final DoubleSetting retention = register(new DoubleSetting("Retention", "Velocity retention factor when airborne and not moving", 0.95, 0.8, 1.0));

    private double lastGroundX = 0;
    private double lastGroundZ = 0;

    public Momentum() {
        super("Momentum", "Keep horizontal momentum in air when not pressing movement keys", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        lastGroundX = 0;
        lastGroundZ = 0;
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;

        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        if (mc.player.isOnGround()) {
            lastGroundX = event.getX();
            lastGroundZ = event.getZ();
            return;
        }

        if (fwd != 0 || side != 0) return;

        double horiz = Math.sqrt(lastGroundX * lastGroundX + lastGroundZ * lastGroundZ);
        if (horiz <= 0) return;

        lastGroundX *= retention.get();
        lastGroundZ *= retention.get();

        event.setX(lastGroundX);
        event.setZ(lastGroundZ);
    }
}
