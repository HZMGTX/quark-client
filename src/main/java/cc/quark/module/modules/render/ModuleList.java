package cc.quark.module.modules.render;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;

public class ModuleList extends Module {

    private final BoolSetting rainbow = register(new BoolSetting("Rainbow", "Rainbow text", true));

    public ModuleList() {
        super("ModuleList", "Lists enabled modules on the right side of the screen", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.getWindow() == null) return;
        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int y = 2;
        int i = 0;
        for (Module module : Quark.getInstance().getModuleManager().getModules()) {
            if (!module.isEnabled() || !module.isVisible()) continue;
            String name = module.getName();
            int width = mc.textRenderer.getWidth(name);
            int color = rainbow.isEnabled() ? ColorUtil.rainbowModule(i) : 0xFFFFFFFF;
            ctx.drawTextWithShadow(mc.textRenderer, name, sw - width - 2, y, color);
            y += mc.textRenderer.fontHeight + 1;
            i++;
        }
    }
}
