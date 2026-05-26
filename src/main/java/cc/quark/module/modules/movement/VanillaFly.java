package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.math.Vec3d;

/**
 * VanillaFly - server-friendly flight that rises and hovers in short bursts to
 * stay within vanilla movement expectations.
 */
public class VanillaFly extends Module {

    private int phase = 0;

    public VanillaFly() {
        super("VanillaFly", "Vanilla-safe hover flight", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        phase = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        Vec3d v = mc.player.getVelocity();
        double y;
        if (mc.options.jumpKey.isPressed()) {
            y = 0.04;
        } else if (mc.options.sneakKey.isPressed()) {
            y = -0.04;
        } else {
            // bob slightly to avoid a perfectly static position
            phase = (phase + 1) % 2;
            y = phase == 0 ? 0.02 : -0.02;
        }
        mc.player.setVelocity(v.x, y, v.z);
        mc.player.fallDistance = 0;
    }
}
