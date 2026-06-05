package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class FPSCounter extends Module {
    private final IntSetting x = register(new IntSetting("X", "X position", 2, 0, 1000));
    private final IntSetting y = register(new IntSetting("Y", "Y position", 2, 0, 600));

    public FPSCounter() { super("FPSCounter", "Shows current FPS on the HUD", Category.RENDER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        DrawContext ctx = e.getDrawContext();
        int fps = mc.getCurrentFps();
        int color = fps >= 60 ? 0xFF55FF55 : (fps >= 30 ? 0xFFFFFF55 : 0xFFFF5555);
        cc.quark.util.RenderUtil.drawCustomText(ctx, fps + " FPS", x.get(), y.get(), color);
    }
}
