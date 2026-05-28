package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class DiagonalSpeed extends Module {

    private final DoubleSetting boost = register(new DoubleSetting("Boost", "Speed boost multiplier when moving diagonally", 1.3, 1.0, 2.0));

    public DiagonalSpeed() {
        super("DiagonalSpeed", "Boost speed when moving diagonally (both axes non-zero)", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;

        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        if (fwd == 0 || side == 0) return;

        double mult = boost.get();
        event.setX(event.getX() * mult);
        event.setZ(event.getZ() * mult);
    }
}
