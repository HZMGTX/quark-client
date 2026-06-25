package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;

public class FluidTracer extends Module {
    private final BoolSetting lava = register(new BoolSetting("Lava", "Trace lava sources", true));
    private final BoolSetting water = register(new BoolSetting("Water", "Trace water sources", false));
    private final IntSetting range = register(new IntSetting("Range", "Trace range", 32, 8, 128));
    public FluidTracer() { super("FluidTracer", "Traces fluid source blocks for navigation", Category.WORLD); }
    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;
    }
}
