package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * FastClimb - climb ladders and vines faster.
 */
public class FastClimb extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Climb speed", 0.25, 0.1, 0.6));

    public FastClimb() {
        super("FastClimb", "Climb ladders faster", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isClimbing()) return;
        Vec3d v = mc.player.getVelocity();
        if (mc.player.input.jumping) {
            mc.player.setVelocity(v.x, speed.get(), v.z);
        } else if (mc.player.input.sneaking) {
            mc.player.setVelocity(v.x, -speed.get(), v.z);
        }
    }
}
