package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * AutoJump - automatically jumps while moving.
 *
 * <ul>
 *   <li><b>Normal</b> - jump every tick the player is on the ground and moving.</li>
 *   <li><b>Bhop</b>   - only jump on the tick of landing (bhop style).</li>
 * </ul>
 */
public class AutoJump extends Module {

    private final BoolSetting bhopMode = register(new BoolSetting(
            "Bhop Mode", "Only jump on the tick of landing (bunny hop)", false));
    private final BoolSetting onlyForward = register(new BoolSetting(
            "Only Forward", "Only auto-jump when walking forward", false));

    private boolean wasInAir = false;

    public AutoJump() {
        super("AutoJump", "Automatically jump while moving on the ground", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasInAir = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();
        boolean moving;

        if (onlyForward.isEnabled()) {
            moving = mc.player.input.movementForward > 0;
        } else {
            moving = mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
        }

        if (!moving) {
            wasInAir = !onGround;
            return;
        }

        if (bhopMode.isEnabled()) {
            // Jump only on the tick of landing
            if (onGround && wasInAir) {
                mc.player.jump();
            }
        } else {
            if (onGround) {
                mc.player.jump();
            }
        }

        wasInAir = !onGround;
    }
}
