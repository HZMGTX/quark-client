package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Downward - apply a constant downward velocity when the sneak key is held
 * while airborne.  Useful for controlled rapid descent without free-fall
 * acceleration.  Optionally zeroes horizontal movement too.
 */
public class Downward extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Downward velocity (blocks/tick)", 0.5, 0.05, 2.0));
    private final BoolSetting stopHorizontal = register(new BoolSetting(
            "Stop Horizontal", "Zero horizontal velocity while descending", false));
    private final BoolSetting alwaysActive = register(new BoolSetting(
            "Always Active", "Descend even without holding sneak", false));

    public Downward() {
        super("Downward", "Controlled rapid descent — hold sneak while airborne", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        boolean active = alwaysActive.isEnabled() || mc.options.sneakKey.isPressed();
        if (!active) return;

        Vec3d v = mc.player.getVelocity();
        double vx = stopHorizontal.isEnabled() ? 0 : v.x;
        double vz = stopHorizontal.isEnabled() ? 0 : v.z;
        mc.player.setVelocity(vx, -speed.get(), vz);
    }
}
