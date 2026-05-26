package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * WaterSpeed - boosts horizontal movement while swimming.
 */
public class WaterSpeed extends Module {

    private final DoubleSetting factor = register(new DoubleSetting(
            "Factor", "Water speed multiplier", 1.8, 1.0, 4.0));

    public WaterSpeed() {
        super("WaterSpeed", "Faster swimming", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isTouchingWater()) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * factor.get(), v.y, v.z * factor.get());
    }
}
