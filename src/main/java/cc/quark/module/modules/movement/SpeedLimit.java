package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * SpeedLimit - caps the player's horizontal speed to a configurable maximum.
 */
public class SpeedLimit extends Module {

    private final DoubleSetting limit = register(new DoubleSetting(
            "Limit", "Maximum horizontal speed (blocks/tick)", 0.3, 0.1, 1.0));

    public SpeedLimit() {
        super("SpeedLimit", "Caps horizontal speed", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        Vec3d v = mc.player.getVelocity();
        double horiz = Math.sqrt(v.x * v.x + v.z * v.z);
        if (horiz > limit.get() && horiz > 0) {
            double scale = limit.get() / horiz;
            mc.player.setVelocity(v.x * scale, v.y, v.z * scale);
        }
    }
}
