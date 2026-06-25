package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class WallJump extends Module {
    private final DoubleSetting jumpPower = register(new DoubleSetting("Power", "Wall jump power", 0.5, 0.1, 1.5));
    private boolean wasAgainstWall = false;

    public WallJump() { super("WallJump", "Jump off walls by pressing jump while against them", Category.MOVEMENT); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        boolean againstWall = mc.player.horizontalCollision;
        if (againstWall && !mc.player.isOnGround() && mc.options.jumpKey.isPressed() && !wasAgainstWall) {
            double yaw = Math.toRadians(mc.player.getYaw());
            mc.player.setVelocity(
                mc.player.getVelocity().x + Math.sin(yaw) * jumpPower.get(),
                jumpPower.get() * 0.8,
                mc.player.getVelocity().z - Math.cos(yaw) * jumpPower.get()
            );
        }
        wasAgainstWall = againstWall;
    }
}
