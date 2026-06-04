package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class StealthWalk extends Module {

    private final BoolSetting crouch = register(new BoolSetting(
            "Crouch", "Crouch while walking to suppress footstep sounds", true));

    public StealthWalk() {
        super("StealthWalk", "Walk without making footstep sounds", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setSneaking(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (crouch.isEnabled()) {
            // Sneaking suppresses step sounds
            mc.player.setSneaking(true);
        } else {
            mc.player.setSneaking(false);
        }
    }
}
