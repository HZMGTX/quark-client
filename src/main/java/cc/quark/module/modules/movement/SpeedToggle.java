package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * SpeedToggle - applies a flat ground speed multiplier while sprinting.
 */
public class SpeedToggle extends Module {

    private final DoubleSetting multiplier = register(new DoubleSetting(
            "Multiplier", "Sprint speed multiplier", 1.4, 1.0, 3.0));

    public SpeedToggle() {
        super("SpeedToggle", "Sprint speed boost", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isSprinting() || !mc.player.isOnGround()) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * multiplier.get(), v.y, v.z * multiplier.get());
    }
}
