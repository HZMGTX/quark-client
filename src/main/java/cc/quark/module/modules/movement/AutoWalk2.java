package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;

/**
 * AutoWalk2 - simulates holding a movement key automatically each tick.
 *
 * <p>The direction is configurable: Forward, Backward, Left, or Right.
 */
public class AutoWalk2 extends Module {

    private final ModeSetting direction = register(new ModeSetting(
            "Direction", "Direction to walk automatically", "Forward",
            "Forward", "Backward", "Left", "Right"));

    public AutoWalk2() {
        super("AutoWalk2", "Walk automatically in a configured direction", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.input.movementForward   = 0.0f;
        mc.player.input.movementSideways  = 0.0f;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        switch (direction.get()) {
            case "Forward"  -> {
                mc.player.input.movementForward  =  1.0f;
                mc.player.input.movementSideways =  0.0f;
            }
            case "Backward" -> {
                mc.player.input.movementForward  = -1.0f;
                mc.player.input.movementSideways =  0.0f;
            }
            case "Left" -> {
                mc.player.input.movementForward  =  0.0f;
                mc.player.input.movementSideways = -1.0f;
            }
            case "Right" -> {
                mc.player.input.movementForward  =  0.0f;
                mc.player.input.movementSideways =  1.0f;
            }
        }
    }
}
