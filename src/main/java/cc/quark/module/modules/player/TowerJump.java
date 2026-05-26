package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * TowerJump - boosts the player upward while holding jump for fast towering.
 */
public class TowerJump extends Module {

    private final DoubleSetting boost = register(new DoubleSetting("Boost", "Upward velocity", 0.42, 0.1, 1.0));

    public TowerJump() {
        super("TowerJump", "Launches the player up while towering", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.options.jumpKey.isPressed() && mc.player.isOnGround()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, boost.get(), v.z);
        }
    }
}
