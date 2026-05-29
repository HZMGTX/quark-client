package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * SpeedLimit - cap horizontal speed at Max (default 5 BPS); clamp velocity
 * magnitude in EventTick. Useful for preventing speed-kick detections.
 */
public class SpeedLimit extends Module {

    private final DoubleSetting max = register(new DoubleSetting(
            "Max", "Maximum horizontal speed (blocks/second ÷ 20 = blocks/tick)", 0.3, 0.05, 1.0));

    public SpeedLimit() {
        super("SpeedLimit", "Clamp horizontal speed to Max blocks/tick", Category.MOVEMENT);
    }

    @Override
    public String getSuffix() {
        return String.format("%.2f", max.get() * 20) + " bps";
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        Vec3d v = mc.player.getVelocity();
        double horiz = Math.sqrt(v.x * v.x + v.z * v.z);
        double limit = max.get();
        if (horiz > limit && horiz > 0) {
            double scale = limit / horiz;
            mc.player.setVelocity(v.x * scale, v.y, v.z * scale);
        }
    }
}
