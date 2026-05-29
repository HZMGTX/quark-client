package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * AutoSprintToggle - automatically keeps the player sprinting whenever they are
 * pressing a movement key (not just forward).  Optionally ignores the hunger
 * check so it also sprints while starving.
 */
public class AutoSprintToggle extends Module {

    private final BoolSetting omnidirectional = register(new BoolSetting(
            "Omni-directional", "Sprint in any direction, not just forward", true));
    private final BoolSetting ignoreHunger = register(new BoolSetting(
            "Ignore Hunger", "Sprint even when hunger is too low for vanilla sprinting", true));

    public AutoSprintToggle() {
        super("AutoSprintToggle", "Always sprint when moving", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.setSprinting(false);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        boolean wantsToMove = omnidirectional.isEnabled()
                ? (fwd != 0 || side != 0)
                : fwd > 0;

        if (!wantsToMove) {
            return; // Don't force-enable sprint when standing still
        }

        boolean canSprint = ignoreHunger.isEnabled()
                || mc.player.getHungerManager().getFoodLevel() > 6;

        if (canSprint) {
            mc.player.setSprinting(true);
        }
    }
}
