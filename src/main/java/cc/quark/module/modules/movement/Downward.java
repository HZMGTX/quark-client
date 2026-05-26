package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Downward - drives the player straight down while holding sneak.
 */
public class Downward extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Downward speed", 0.5, 0.1, 1.5));

    public Downward() {
        super("Downward", "Descend straight down", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.options.sneakKey.isPressed()) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x, -speed.get(), v.z);
    }
}
