package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * FastFall - push the player downward at a configurable speed when sneaking
 * while airborne, enabling rapid descent.
 *
 * <p>The {@code No Fall} setting resets fall distance so landing after a fast
 * fall does not deal damage.
 */
public class FastFall extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Downward velocity while fast-falling (blocks/tick)", 0.5, 0.1, 3.0));
    private final BoolSetting noFall = register(new BoolSetting(
            "No Fall", "Reset fall distance to avoid fall damage on landing", true));
    private final BoolSetting requireSneak = register(new BoolSetting(
            "Require Sneak", "Only fast-fall when sneak key is held", true));

    public FastFall() {
        super("FastFall", "Rapid descent when sneaking airborne", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        if (requireSneak.isEnabled() && !mc.options.sneakKey.isPressed()) return;

        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x, -speed.get(), v.z);

        if (noFall.isEnabled()) {
            mc.player.fallDistance = 0;
        }
    }
}
