package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * CrouchFly - levitate while sneaking, using vertical keys for altitude.
 */
public class CrouchFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Vertical speed", 0.3, 0.1, 1.0));

    public CrouchFly() {
        super("CrouchFly", "Fly while sneaking", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isSneaking()) return;
        Vec3d v = mc.player.getVelocity();
        double y = 0.0;
        if (mc.options.jumpKey.isPressed()) y = speed.get();
        mc.player.setVelocity(v.x, y, v.z);
    }
}
