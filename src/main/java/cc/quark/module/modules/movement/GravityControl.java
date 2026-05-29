package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;

/**
 * GravityControl - multiplies (or replaces) the Y component of the move event
 * while the player is airborne, allowing custom gravity strengths.
 *
 * <ul>
 *   <li><b>Reduce</b>  - halve downward Y (lighter gravity).</li>
 *   <li><b>Zero</b>    - zero all Y velocity (zero-G hover).</li>
 *   <li><b>Reverse</b> - negate Y velocity (fall upward).</li>
 *   <li><b>Custom</b>  - multiply Y by the {@code Factor} setting.</li>
 * </ul>
 */
public class GravityControl extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Gravity mode", "Reduce", "Reduce", "Zero", "Reverse", "Custom"));
    private final DoubleSetting factor = register(new DoubleSetting(
            "Factor", "Y multiplier in Custom mode (0=zero-G, 1=normal, 2=double, -1=reverse)", 0.5, -2.0, 3.0));

    public GravityControl() {
        super("GravityControl", "Adjust gravity strength while airborne", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        double currentY = event.getY();

        switch (mode.get()) {
            case "Reduce" -> {
                if (currentY < 0) event.setY(currentY * 0.4);
            }
            case "Zero"    -> event.setY(0.0);
            case "Reverse" -> event.setY(-currentY);
            case "Custom"  -> event.setY(currentY * factor.get());
        }

        if (event.getY() >= 0 || mode.is("Reverse")) {
            mc.player.fallDistance = 0;
        }
    }
}
