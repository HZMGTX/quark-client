package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Vec3d;

public class NoFriction extends Module {

    private final BoolSetting onlyOnGround = register(new BoolSetting(
            "OnlyOnGround", "Only remove friction while on the ground", true));

    private double lastVx = 0;
    private double lastVz = 0;

    public NoFriction() {
        super("NoFriction", "Removes ground friction for ice-like movement", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        lastVx = 0;
        lastVz = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();
        if (onlyOnGround.isEnabled() && !onGround) {
            lastVx = mc.player.getVelocity().x;
            lastVz = mc.player.getVelocity().z;
            return;
        }

        Vec3d vel = mc.player.getVelocity();
        boolean moving = mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;

        if (!moving && onGround) {
            // Preserve momentum instead of applying friction
            mc.player.setVelocity(lastVx, vel.y, lastVz);
        } else {
            lastVx = vel.x;
            lastVz = vel.z;
        }
    }
}
