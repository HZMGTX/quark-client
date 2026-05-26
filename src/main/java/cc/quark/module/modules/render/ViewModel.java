package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class ViewModel extends Module {

    private final IntSetting x = register(new IntSetting("X", "X pos", 4, 0, 500));
    private final IntSetting y = register(new IntSetting("Y", "Y pos", 4, 0, 500));

    public ViewModel() {
        super("ViewModel", "Shows the current camera perspective on the HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.options == null) return;
        DrawContext ctx = event.getDrawContext();
        String persp = mc.options.getPerspective().name();
        ctx.drawTextWithShadow(mc.textRenderer, "View: " + persp, x.get(), y.get(), 0xFFFFFFFF);
    }
}
