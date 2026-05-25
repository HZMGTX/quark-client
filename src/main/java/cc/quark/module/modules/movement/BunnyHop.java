package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class BunnyHop extends Module {

    private final DoubleSetting boost = register(new DoubleSetting(
            "Speed Boost", "Horizontal velocity multiplier on each hop", 1.02, 1.0, 1.3));
    private final BoolSetting autoSprint = register(new BoolSetting(
            "Auto Sprint", "Keep sprinting while hopping", true));

    public BunnyHop() {
        super("BunnyHop", "Jump immediately on landing to carry momentum (Vape-style)", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isSneaking()) return;

        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;
        if (!moving) return;

        if (autoSprint.isEnabled()) mc.player.setSprinting(true);

        if (mc.player.isOnGround()) {
            mc.player.jump();
            Vec3d vel = mc.player.getVelocity();
            double b = boost.get();
            mc.player.setVelocity(vel.x * b, vel.y, vel.z * b);
        }
    }
}
