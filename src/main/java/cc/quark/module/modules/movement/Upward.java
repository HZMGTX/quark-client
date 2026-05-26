package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Upward - drives the player straight up while holding jump in the air.
 */
public class Upward extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Upward speed", 0.5, 0.1, 1.5));

    public Upward() {
        super("Upward", "Ascend straight up", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.options.jumpKey.isPressed() || mc.player.isOnGround()) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x, speed.get(), v.z);
    }
}
