package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class FpsDisplay extends Module {

    private final IntSetting x = register(new IntSetting("X", "X pos", 4, 0, 500));
    private final IntSetting y = register(new IntSetting("Y", "Y pos", 4, 0, 500));

    public FpsDisplay() {
        super("FpsDisplay", "Shows the current frames per second", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        DrawContext ctx = event.getDrawContext();
        ctx.drawTextWithShadow(mc.textRenderer, "FPS: " + mc.getCurrentFps(), x.get(), y.get(), 0xFFFFFFFF);
    }
}
