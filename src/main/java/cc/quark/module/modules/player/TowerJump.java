package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class TowerJump extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Jump Y velocity multiplier", 1.0, 1.0, 3.0));

    private boolean wasOnGround = false;

    public TowerJump() {
        super("TowerJump", "Rapidly jump upward while holding space", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        wasOnGround = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.options.jumpKey.isPressed()) return;

        boolean onGround = mc.player.isOnGround();

        if (onGround) {
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, 0.42 * speed.get(), vel.z);
            mc.player.jump();
        } else if (!wasOnGround) {
            Vec3d vel = mc.player.getVelocity();
            if (vel.y > 0) {
                mc.player.setVelocity(vel.x, vel.y * Math.min(speed.get(), 1.5), vel.z);
            }
        }

        wasOnGround = onGround;
    }
}
