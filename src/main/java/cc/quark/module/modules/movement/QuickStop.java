package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.math.Vec3d;

/**
 * QuickStop - instantly halts horizontal motion when no movement key is held.
 */
public class QuickStop extends Module {

    public QuickStop() {
        super("QuickStop", "Stop instantly when idle", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        boolean moving = mc.player.input.movementForward != 0
                || mc.player.input.movementSideways != 0;
        if (moving) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(0.0, v.y, 0.0);
    }
}
