package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;

/**
 * SneakPersist — keeps the player in a permanent sneak state without needing
 * to hold the shift key. Useful for staying on ledges or silent movement.
 */
public class SneakPersist extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "How to apply the sneak state",
            "Toggle", "Toggle", "Hold"));

    private final BoolSetting stopOnSprint = register(new BoolSetting(
            "Stop On Sprint", "Temporarily release sneak while sprinting", true));

    private final BoolSetting stopWhileGui = register(new BoolSetting(
            "Stop In GUI", "Release sneak when a GUI is open", true));

    public SneakPersist() {
        super("SneakPersist", "Keeps sneak held without pressing shift", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        // Release sneak when the module is disabled
        if (mc.options != null) {
            mc.options.sneakKey.setPressed(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean shouldSneak = true;

        // Pause sneak while GUI open
        if (stopWhileGui.isEnabled() && mc.currentScreen != null) {
            shouldSneak = false;
        }

        // Pause sneak while sprinting
        if (stopOnSprint.isEnabled() && mc.player.isSprinting()) {
            shouldSneak = false;
        }

        if (mode.get().equals("Hold")) {
            // In Hold mode: only sneak while module is enabled (acts as auto-hold)
            mc.options.sneakKey.setPressed(shouldSneak);
        } else {
            // Toggle mode: continuously force sneak on
            mc.options.sneakKey.setPressed(shouldSneak);
        }

        // The sneakKey.setPressed(true) is sufficient to keep the sneak state;
        // the vanilla input handler reads this key state each tick.
    }
}
