package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.math.Vec3d;

/**
 * YPort - NCP-style speed exploiting the vertical (Y) port mechanic by jumping
 * higher and gaining horizontal distance on the way up.
 */
public class YPort extends Module {

    private boolean wasOnGround = false;

    public YPort() {
        super("YPort", "Vertical-port based speed", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasOnGround = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.input.movementForward == 0 && mc.player.input.movementSideways == 0) return;
        boolean onGround = mc.player.isOnGround();
        Vec3d v = mc.player.getVelocity();
        if (onGround && !wasOnGround) {
            mc.player.setVelocity(v.x, 0.42, v.z);
        } else if (!onGround) {
            mc.player.setVelocity(v.x * 1.18, v.y, v.z * 1.18);
        }
        wasOnGround = onGround;
    }
}
