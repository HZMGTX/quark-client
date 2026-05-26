package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Tower - shoots the player straight up while holding jump.
 */
public class Tower extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Upward speed", 0.42, 0.1, 0.9));

    public Tower() {
        super("Tower", "Rise straight up", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.options.jumpKey.isPressed()) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x, speed.get(), v.z);
    }
}
