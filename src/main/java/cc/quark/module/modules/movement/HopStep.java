package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventJump;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * HopStep - adds extra horizontal speed at the start of a jump.
 */
public class HopStep extends Module {

    private final DoubleSetting boost = register(new DoubleSetting(
            "Boost", "Horizontal boost multiplier", 1.3, 1.0, 2.0));

    public HopStep() {
        super("HopStep", "Boosts jumps horizontally", Category.MOVEMENT);
    }

    @EventHandler
    public void onJump(EventJump event) {
        if (mc.player == null) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * boost.get(), v.y, v.z * boost.get());
    }
}
