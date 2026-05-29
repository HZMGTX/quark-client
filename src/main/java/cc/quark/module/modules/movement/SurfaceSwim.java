package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * SurfaceSwim - keeps the player exactly at the water surface without sinking.
 * Monitors when the player is touching water and sets Y velocity to hold them
 * at the surface level.
 */
public class SurfaceSwim extends Module {

    private final DoubleSetting surfaceOffset = register(new DoubleSetting(
            "Surface Offset", "Y offset above water surface to maintain (blocks)", 0.0, -0.5, 0.5));
    private final BoolSetting allowJump = register(new BoolSetting(
            "Allow Jump", "Let the player jump off the surface with space", true));

    public SurfaceSwim() {
        super("SurfaceSwim", "Swim on the water surface without sinking", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isTouchingWater()) return;

        // If jump is pressed and allowed, let it pass
        if (allowJump.isEnabled() && mc.player.input.jumping) return;

        Vec3d vel = mc.player.getVelocity();

        // Cancel downward Y velocity to prevent sinking
        if (vel.y < 0) {
            mc.player.setVelocity(vel.x, surfaceOffset.get() * 0.1, vel.z);
        }

        mc.player.fallDistance = 0;
    }
}
