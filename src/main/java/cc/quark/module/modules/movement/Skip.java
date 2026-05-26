package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Skip - small periodic hops while walking on the ground.
 */
public class Skip extends Module {

    private final DoubleSetting height = register(new DoubleSetting(
            "Height", "Hop height", 0.2, 0.05, 0.42));
    private int counter;

    public Skip() {
        super("Skip", "Periodic ground hops", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;
        if (++counter % 10 != 0) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x, height.get(), v.z);
    }
}
