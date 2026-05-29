package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * FastClimb - dramatically increases ladder/vine climbing speed.
 *
 * <p>When on a climbable block:
 * <ul>
 *   <li>Jump key held → ascend at {@code speed} blocks/tick.</li>
 *   <li>Sneak key held → descend at {@code speed} blocks/tick.</li>
 *   <li>Neither → ascend by default (vanilla climbs upward by default too).</li>
 * </ul>
 * Horizontal velocity is preserved so the player can still move left/right while climbing.
 */
public class FastClimb extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Climb speed (blocks/tick)", 0.3, 0.05, 1.0));
    private final BoolSetting defaultAscend = register(new BoolSetting(
            "Default Ascend", "Climb up by default when no key is pressed", true));
    private final BoolSetting noFallDamage = register(new BoolSetting(
            "No Fall Damage", "Reset fall distance while on a climbable", true));

    public FastClimb() {
        super("FastClimb", "Climb ladders and vines at full configurable speed", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isClimbing()) return;

        Vec3d v = mc.player.getVelocity();

        boolean jumpHeld  = mc.player.input.jumping;
        boolean sneakHeld = mc.player.input.sneaking;

        double vy;
        if (sneakHeld) {
            vy = -speed.get();
        } else if (jumpHeld || defaultAscend.isEnabled()) {
            vy = speed.get();
        } else {
            vy = v.y; // leave vanilla speed
        }

        mc.player.setVelocity(v.x, vy, v.z);

        if (noFallDamage.isEnabled()) {
            mc.player.fallDistance = 0;
        }
    }
}
