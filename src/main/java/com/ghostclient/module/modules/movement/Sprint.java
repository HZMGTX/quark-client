package com.ghostclient.module.modules.movement;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.EnumSetting;

/**
 * Sprint - forces the player to sprint in various directions.
 *
 * <p>Modes:
 * <ul>
 *   <li><b>Legit</b>  - sprints only when moving forward, mimicking vanilla behaviour.</li>
 *   <li><b>Omni</b>   - sprints in any horizontal direction including backwards and sideways.</li>
 *   <li><b>Always</b> - calls setSprinting(true) every tick unconditionally.</li>
 * </ul>
 *
 * <p>noStop: re-enables sprinting immediately after knockback resets it.
 */
public class Sprint extends Module {

    public enum SprintMode {
        LEGIT, OMNI, ALWAYS
    }

    private final EnumSetting<SprintMode> mode = register(new EnumSetting<>(
            "Mode", "Sprint method", SprintMode.OMNI));

    private final BoolSetting noStop = register(new BoolSetting(
            "No Stop", "Re-enable sprint immediately after knockback or hit resets it", true));

    // Track sprinting state from last tick for re-enable logic
    private boolean wasSprintingLastTick = false;

    public Sprint() {
        super("Sprint", "Forces the player to sprint", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasSprintingLastTick = false;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isSneaking()) return;
        if (mc.player.isSubmergedInWater()) return;
        // Vanilla requires food level > 6 to sprint
        if (mc.player.getHungerManager().getFoodLevel() <= 6) return;

        boolean moving = mc.player.input.movementForward  != 0
                      || mc.player.input.movementSideways != 0;

        // noStop: if we were sprinting last tick but aren't now (e.g. knockback reset it),
        // force sprinting back on immediately as long as we are moving.
        if (noStop.isEnabled() && wasSprintingLastTick && !mc.player.isSprinting() && moving) {
            mc.player.setSprinting(true);
        }

        switch (mode.get()) {
            case ALWAYS -> mc.player.setSprinting(true);
            case LEGIT  -> {
                if (mc.player.input.movementForward > 0) {
                    mc.player.setSprinting(true);
                }
            }
            case OMNI -> {
                if (moving) {
                    mc.player.setSprinting(true);
                }
            }
        }

        wasSprintingLastTick = mc.player.isSprinting();
    }
}
