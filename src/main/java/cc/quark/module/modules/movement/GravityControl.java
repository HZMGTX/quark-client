package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * GravityControl - scales downward velocity to soften or strengthen gravity.
 */
public class GravityControl extends Module {

    private final DoubleSetting factor = register(new DoubleSetting(
            "Factor", "Vertical velocity scale", 0.6, 0.0, 1.0));

    public GravityControl() {
        super("GravityControl", "Adjusts fall gravity", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;
        Vec3d v = mc.player.getVelocity();
        if (v.y < 0) {
            mc.player.setVelocity(v.x, v.y * factor.get(), v.z);
        }
    }
}
