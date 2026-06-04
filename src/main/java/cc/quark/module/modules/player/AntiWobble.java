package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class AntiWobble extends Module {

    private final BoolSetting always = register(new BoolSetting(
            "Always", "Always prevent camera wobble, not only on damage", true));

    public AntiWobble() {
        super("AntiWobble", "Prevents camera wobble from damage", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (always.isEnabled() || mc.player.hurtTime > 0) {
            mc.player.hurtTime = 0;
            // Reset hurt yaw so the camera doesn't rotate on damage
            mc.player.hurtDirectionYaw = 0f;
        }
    }
}
