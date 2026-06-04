package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class SprintJump extends Module {

    private final DoubleSetting boost = register(new DoubleSetting(
            "Boost", "Speed multiplier applied on sprint-jump", 1.15, 1.0, 3.0));

    private final BoolSetting autoTime = register(new BoolSetting(
            "Auto Time", "Auto-time jumps for optimal parkour momentum", true));

    private boolean wasOnGround = false;

    public SprintJump() {
        super("SprintJump", "Enhanced sprint-jumping for parkour", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasOnGround = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();
        boolean jumping = mc.options.jumpKey.isPressed();
        boolean sprinting = mc.player.isSprinting();
        boolean moving = mc.player.input.movementForward > 0;

        if (moving) {
            mc.player.setSprinting(true);
        }

        // On landing, apply horizontal boost
        if (onGround && !wasOnGround && sprinting && moving) {
            Vec3d vel = mc.player.getVelocity();
            double b = boost.get();
            mc.player.setVelocity(vel.x * b, vel.y, vel.z * b);
        }

        // Auto-time: jump as soon as we land if jump key is held
        if (autoTime.isEnabled() && onGround && jumping && moving) {
            mc.player.jump();
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x * boost.get(), vel.y, vel.z * boost.get());
        }

        wasOnGround = onGround;
    }
}
