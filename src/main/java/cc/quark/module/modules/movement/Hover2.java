package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.math.Vec3d;

/**
 * Hover2 - keeps the player suspended in mid-air with zero vertical velocity.
 */
public class Hover2 extends Module {

    public Hover2() {
        super("Hover2", "Suspend in mid-air", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * 0.9, 0.0, v.z * 0.9);
    }
}
