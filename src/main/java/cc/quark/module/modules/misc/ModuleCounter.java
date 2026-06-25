package cc.quark.module.modules.misc;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class ModuleCounter extends Module {

    private final IntSetting x = register(new IntSetting("X", "HUD X position", 4, 0, 3840));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 200, 0, 2160));

    public ModuleCounter() {
        super("ModuleCounter", "Shows number of currently enabled modules on HUD", Category.MISC);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        long count = Quark.getInstance().getModuleManager().getModules().stream()
                .filter(m -> m.isEnabled() && m != this).count();
        ctx.drawTextWithShadow(mc.textRenderer, "Modules: " + count, x.get(), y.get(), 0xFFAAAAAA);
    }
}
