package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;

public class DeathMarker2 extends Module {
    private final BoolSetting showTracers = register(new BoolSetting("Tracers", "Show tracers", false));
    private final IntSetting color = register(new IntSetting("Color", "Color as ARGB int", 0xFF00FFFF, Integer.MIN_VALUE, Integer.MAX_VALUE));

    public DeathMarker2() { super("DeathMarker2", "Advanced death marker with timestamp", Category.RENDER); }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
    }
}
