package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class AspectRatio extends Module {

    private final IntSetting x = register(new IntSetting("X", "X pos", 4, 0, 500));
    private final IntSetting y = register(new IntSetting("Y", "Y pos", 4, 0, 500));

    public AspectRatio() {
        super("AspectRatio", "Displays the current screen aspect ratio", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.getWindow() == null) return;
        DrawContext ctx = event.getDrawContext();
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();
        double ratio = h == 0 ? 0 : (double) w / h;
        ctx.drawTextWithShadow(mc.textRenderer, String.format("Ratio: %.2f", ratio), x.get(), y.get(), 0xFFFFFFFF);
    }
}
