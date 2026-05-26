package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * LavaSpeed - boosts horizontal movement while inside lava.
 */
public class LavaSpeed extends Module {

    private final DoubleSetting factor = register(new DoubleSetting(
            "Factor", "Lava speed multiplier", 2.0, 1.0, 4.0));

    public LavaSpeed() {
        super("LavaSpeed", "Faster lava travel", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isInLava()) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * factor.get(), v.y, v.z * factor.get());
    }
}
