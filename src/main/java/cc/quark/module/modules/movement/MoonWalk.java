package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventJump;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class MoonWalk extends Module {

    private final DoubleSetting gravity = register(new DoubleSetting(
            "Gravity", "Gravity factor (lower = floatier)", 0.3, 0.05, 1.0));

    public MoonWalk() {
        super("MoonWalk", "Simulate low gravity moon walking", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;
        if (mc.player.isTouchingWater() || mc.player.isInLava()) return;

        Vec3d vel = mc.player.getVelocity();
        if (vel.y < 0) {
            mc.player.setVelocity(vel.x, vel.y * gravity.get(), vel.z);
        }
    }

    @EventHandler
    public void onJump(EventJump event) {
        if (mc.player == null) return;
        Vec3d vel = mc.player.getVelocity();
        double jumpBoost = 0.42 * (2.0 - gravity.get());
        mc.player.setVelocity(vel.x, Math.min(jumpBoost, 1.5), vel.z);
    }
}
