package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * SmoothFly - simple horizontal flight aligned to the look direction.
 */
public class SmoothFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Fly speed", 0.5, 0.1, 2.0));

    public SmoothFly() {
        super("SmoothFly", "Smooth horizontal flight", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        double y = 0.0;
        if (mc.options.jumpKey.isPressed()) y = speed.get();
        else if (mc.options.sneakKey.isPressed()) y = -speed.get();
        double mx = 0, mz = 0;
        if (mc.player.input.movementForward != 0) {
            float yaw = mc.player.getYaw();
            mx = -MathHelper.sin(yaw * 0.017453292f) * speed.get();
            mz = MathHelper.cos(yaw * 0.017453292f) * speed.get();
        }
        Vec3d ignored = mc.player.getVelocity();
        mc.player.setVelocity(mx, y, mz);
    }
}
