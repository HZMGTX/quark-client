package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * FastDescend - makes the player fall or descend much faster.
 *
 * <p>When {@code Sneak Activate} is enabled the effect only triggers while the
 * sneak key is held during a fall, acting as an on-demand fast-drop.  When
 * disabled, downward velocity is boosted every tick while airborne and moving
 * downward.
 */
public class FastDescend extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Downward velocity override (blocks/tick)", 2.0, 0.5, 10.0));

    private final BoolSetting sneakActivate = register(new BoolSetting(
            "Sneak Activate", "Only descend fast while sneak key is held", true));

    private final BoolSetting noFallDamage = register(new BoolSetting(
            "No Fall Damage", "Reset fall distance to prevent damage on landing", false));

    public FastDescend() {
        super("FastDescend", "Fall/descend much faster", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        if (sneakActivate.isEnabled() && !mc.options.sneakKey.isPressed()) return;

        Vec3d vel = mc.player.getVelocity();
        // Only apply when not ascending
        if (vel.y > 0.1) return;

        mc.player.setVelocity(vel.x, -speed.get(), vel.z);

        if (noFallDamage.isEnabled()) {
            mc.player.fallDistance = 0;
        }
    }
}
