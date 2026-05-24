package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Vec3d;

/**
 * FastLadder - dramatically increases ladder/vine climbing speed by overriding the
 * player's upward velocity each tick while they are on a climbable block.
 *
 * <p>Vanilla ladder speed is capped at ~0.118 blocks/tick upward.  This module
 * replaces that with a configurable value.
 */
public class FastLadder extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Climb speed in blocks/tick (vanilla â‰ˆ 0.12)", 0.4, 0.1, 1.0));

    public FastLadder() {
        super("FastLadder", "Increases ladder and vine climbing speed", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // Check if the player is currently on a climbable block (ladder, vine, etc.)
        if (!mc.player.isClimbing()) return;

        // Determine direction: up if jump key held, down if sneak, up by default while moving
        Vec3d vel = mc.player.getVelocity();

        if (mc.options.sneakKey.isPressed()) {
            // Descend fast
            mc.player.setVelocity(vel.x, -speed.get(), vel.z);
        } else {
            // Ascend fast
            mc.player.setVelocity(vel.x, speed.get(), vel.z);
        }
    }
}
