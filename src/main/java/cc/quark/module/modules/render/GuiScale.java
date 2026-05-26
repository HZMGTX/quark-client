package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class GuiScale extends Module {

    private final IntSetting x = register(new IntSetting("X", "X pos", 4, 0, 500));
    private final IntSetting y = register(new IntSetting("Y", "Y pos", 4, 0, 500));

    public GuiScale() {
        super("GuiScale", "Displays the current GUI scale factor", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.getWindow() == null) return;
        DrawContext ctx = event.getDrawContext();
        double scale = mc.getWindow().getScaleFactor();
        ctx.drawTextWithShadow(mc.textRenderer, String.format("GUI Scale: %.0f", scale), x.get(), y.get(), 0xFFFFFFFF);
    }
}
