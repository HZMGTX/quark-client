package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

/**
 * GroundSpeed - applies a horizontal speed boost exclusively while the player
 * is on the ground, similar to a Speed effect but without the airborne boost.
 *
 * <ul>
 *   <li><b>Walk</b>   - boost when moving at walking speed (not sprinting).</li>
 *   <li><b>Sprint</b> - boost when sprinting.</li>
 *   <li><b>Both</b>   - boost regardless of sprint state.</li>
 * </ul>
 */
public class GroundSpeed extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "When to apply boost", "Both", "Both", "Walk", "Sprint"));
    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Horizontal velocity multiplier on ground", 1.5, 1.0, 4.0));
    private final BoolSetting noJump = register(new BoolSetting(
            "No Jump", "Prevent jumping so the player stays on the ground", false));

    public GroundSpeed() {
        super("GroundSpeed", "Speed boost only while on the ground", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;

        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;
        if (!moving) return;

        boolean sprinting = mc.player.isSprinting();

        boolean apply = switch (mode.get()) {
            case "Walk"   -> !sprinting;
            case "Sprint" -> sprinting;
            default       -> true; // "Both"
        };

        if (!apply) return;

        // Prevent jumping if the option is enabled
        if (noJump.isEnabled() && mc.player.input.jumping) {
            mc.player.input.jumping = false;
        }

        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * speed.get(), v.y, v.z * speed.get());
    }
}
