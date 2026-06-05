package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;

public class SkyColor extends Module {
    private final ColorSetting color = register(new ColorSetting("Color", "Sky color override", 0xFF87CEEB));
    private final BoolSetting rainbow = register(new BoolSetting("Rainbow", "Cycle rainbow sky", false));

    public SkyColor() { super("SkyColor", "Changes sky color to custom color", Category.RENDER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        // Sky color injection happens through rendering mixins
    }
}
