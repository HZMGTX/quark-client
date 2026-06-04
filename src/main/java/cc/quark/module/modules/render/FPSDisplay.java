package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class FPSDisplay extends Module {
    private final IntSetting x = register(new IntSetting("X","HUD X position",2,0,3840));
    private final IntSetting y = register(new IntSetting("Y","HUD Y position",20,0,2160));
    public FPSDisplay() { super("FPSDisplay","Shows current FPS on the HUD",Category.RENDER); }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player==null) return;
        DrawContext ctx = event.getDrawContext();
        int fps = mc.getCurrentFps();
        int col = fps>=60?0xFF55FF55:fps>=30?0xFFFFFF55:0xFFFF5555;
        ctx.drawTextWithShadow(mc.textRenderer,"FPS: "+fps,x.get(),y.get(),col);
    }
}
