package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class NoSway extends Module {

    private final BoolSetting weapons = register(new BoolSetting(
            "Weapons", "Remove weapon sway animation", true));

    private final BoolSetting items = register(new BoolSetting(
            "Items", "Remove item sway animation", true));

    public NoSway() {
        super("NoSway", "Removes weapon sway animation", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        // Sway suppression is handled via mixin; this tick handler holds the enabled state
        // and can zero out the player's previous pitch delta which drives arm sway.
        if (weapons.isEnabled() || items.isEnabled()) {
            // Force prevPitch == pitch to suppress sway calculation
            mc.player.prevPitch = mc.player.getPitch();
            mc.player.prevYaw = mc.player.getYaw();
        }
    }
}
