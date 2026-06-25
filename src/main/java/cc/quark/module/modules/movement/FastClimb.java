package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class FastClimb extends Module {
    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Climb speed", 0.3, 0.1, 2.0));

    public FastClimb() { super("FastClimb", "Climb ladders and vines faster", Category.MOVEMENT); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        if (mc.player.isClimbing()) {
            Vec3d vel = mc.player.getVelocity();
            double climbSpeed = speed.get();
            if (mc.options.jumpKey.isPressed()) {
                mc.player.setVelocity(vel.x, climbSpeed, vel.z);
            } else if (mc.options.sneakKey.isPressed()) {
                mc.player.setVelocity(vel.x, -climbSpeed, vel.z);
            } else if (mc.options.forwardKey.isPressed()) {
                mc.player.setVelocity(vel.x, climbSpeed, vel.z);
            }
        }
    }
}
