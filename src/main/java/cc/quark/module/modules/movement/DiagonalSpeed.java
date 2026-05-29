package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

/**
 * DiagonalSpeed - applies an extra speed multiplier when the player is moving
 * diagonally (both forward/backward AND left/right inputs are non-zero).
 *
 * <p>Diagonal movement covers the 45° angles where the input vectors add up to
 * a longer combined vector, giving an overall speed boost.
 */
public class DiagonalSpeed extends Module {

    private final DoubleSetting boost = register(new DoubleSetting(
            "Boost", "Speed multiplier applied during diagonal movement", 1.3, 1.0, 3.0));
    private final BoolSetting normalizeFirst = register(new BoolSetting(
            "Normalize", "Normalize input before boosting to avoid asymmetric speeds", true));

    public DiagonalSpeed() {
        super("DiagonalSpeed", "Extra speed boost when moving diagonally", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        // Only diagonal movement
        if (fwd == 0 || side == 0) return;

        double mult = boost.get();

        if (normalizeFirst.isEnabled()) {
            // Normalise the XZ vector first so we apply a clean multiplier
            double len = Math.sqrt(event.getX() * event.getX() + event.getZ() * event.getZ());
            if (len > 0) {
                // Direction of movement
                double nx = event.getX() / len;
                double nz = event.getZ() / len;
                // Re-apply at target speed
                double targetLen = len * mult;
                event.setX(nx * targetLen);
                event.setZ(nz * targetLen);
            }
        } else {
            event.setX(event.getX() * mult);
            event.setZ(event.getZ() * mult);
        }
    }
}
