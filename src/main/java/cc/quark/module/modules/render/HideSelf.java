package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class HideSelf extends Module {
    private final BoolSetting hideThirdPerson = register(new BoolSetting("HideThirdPerson", "Hide in third person too", false));
    public HideSelf() { super("HideSelf", "Hides your own player model from rendering", Category.RENDER); }
    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null) return;
        if (!hideThirdPerson.getValue() && mc.options.getPerspective().isFirstPerson()) return;
        mc.player.setInvisible(true);
    }
    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.setInvisible(false);
    }
}
