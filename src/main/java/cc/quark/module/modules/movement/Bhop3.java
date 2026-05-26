package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Bhop3 - auto-jumps on the ground and keeps horizontal momentum.
 */
public class Bhop3 extends Module {

    private final DoubleSetting boost = register(new DoubleSetting(
            "Boost", "Horizontal boost", 1.1, 1.0, 1.5));

    public Bhop3() {
        super("Bhop3", "Auto bunny hop", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        boolean moving = mc.player.input.movementForward != 0
                || mc.player.input.movementSideways != 0;
        if (!moving || !mc.player.isOnGround()) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * boost.get(), 0.42, v.z * boost.get());
    }
}
