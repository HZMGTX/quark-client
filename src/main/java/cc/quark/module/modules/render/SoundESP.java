package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class SoundESP extends Module {
    private final DoubleSetting range = register(new DoubleSetting("Range", "Sound detection range", 20.0, 5.0, 64.0));
    private final BoolSetting showFootsteps = register(new BoolSetting("Footsteps", "Show footstep sounds", true));
    public SoundESP() { super("SoundESP", "Visualizes nearby sounds on screen", Category.RENDER); }
    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
    }
}
