package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * VelocityBoost - amplifies knockback received for faster repositioning.
 */
public class VelocityBoost extends Module {

    private final DoubleSetting factor = register(new DoubleSetting(
            "Factor", "Knockback amplify", 1.5, 1.0, 3.0));

    public VelocityBoost() {
        super("VelocityBoost", "Amplifies knockback", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.hurtTime <= 0) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * factor.get(), v.y, v.z * factor.get());
    }
}
