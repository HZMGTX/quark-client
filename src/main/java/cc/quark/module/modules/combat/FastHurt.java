package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;

/**
 * FastHurt — reduces or removes invincibility frames (hurtTime) so enemies
 * can hit the player again faster.  Typically used with AutoTotem.
 *
 * Modes:
 *   Always    — zero out hurtTime every tick unconditionally.
 *   In Combat — only zero hurtTime for 3 seconds after last hit.
 *   Custom    — set hurtTime to a specific value instead of 0.
 */
public class FastHurt extends Module {

    private final ModeSetting mode    = register(new ModeSetting(
            "Mode", "When to clear invincibility frames", "Always", "Always", "In Combat", "Custom"));
    private final IntSetting  minTime = register(new IntSetting(
            "Min Hurt Time", "Target hurtTime value (Custom mode only)", 0, 0, 10));

    private int combatTimer = 0;

    public FastHurt() {
        super("FastHurt", "Reduces invincibility frames so you can be hit in rapid succession", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        combatTimer = 0;
    }

    @Override
    public String getSuffix() {
        return mode.get();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (mc.player.hurtTime > 0) {
            combatTimer = 60; // 3 seconds
        }

        boolean active = switch (mode.get()) {
            case "Always"    -> true;
            case "In Combat" -> combatTimer > 0;
            default          -> true; // Custom
        };

        if (combatTimer > 0) combatTimer--;

        if (!active) return;

        int target = mode.is("Custom") ? minTime.get() : 0;
        if (mc.player.hurtTime > target) {
            mc.player.hurtTime = target;
        }
    }
}
