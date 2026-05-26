package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * SneakSpeed - move at near-normal speed while sneaking.
 */
public class SneakSpeed extends Module {

    private final DoubleSetting factor = register(new DoubleSetting(
            "Factor", "Sneak speed multiplier", 2.5, 1.0, 5.0));

    public SneakSpeed() {
        super("SneakSpeed", "Faster sneaking", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isSneaking() || !mc.player.isOnGround()) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * factor.get(), v.y, v.z * factor.get());
    }
}
