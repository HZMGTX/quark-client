package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class NoHurtCam extends Module {

    public static NoHurtCam INSTANCE;

    private final BoolSetting noRedOverlay = register(new BoolSetting("NoRedOverlay", "Also removes the red damage tint", true));

    public NoHurtCam() {
        super("NoHurtCam", "Removes screen shake and red overlay when taking damage", Category.RENDER);
        INSTANCE = this;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        // tiltViewWhenHurt is cancelled via MixinGameRenderer; zero hurtTime to
        // also suppress the red damage overlay when the setting is active
        if (noRedOverlay.isEnabled()) {
            mc.player.hurtTime = 0;
        }
    }
}
