package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.math.Vec3d;

/**
 * FloatHack - holds the player at a constant height while in the air.
 */
public class FloatHack extends Module {

    public FloatHack() {
        super("FloatHack", "Hover at current height", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x, 0.0, v.z);
    }
}
