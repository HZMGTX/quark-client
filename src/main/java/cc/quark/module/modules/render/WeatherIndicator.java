package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.client.gui.DrawContext;

public class WeatherIndicator extends Module {
    public WeatherIndicator() { super("WeatherIndicator", "Shows current weather on HUD", Category.RENDER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.world == null) return;
        DrawContext ctx = e.getDrawContext();
        String weather;
        int color;
        if (mc.world.isThundering()) { weather = "⚡ Thunder"; color = 0xFFFFFF55; }
        else if (mc.world.isRaining()) { weather = "🌧 Rain"; color = 0xFF5599FF; }
        else { weather = "☀ Clear"; color = 0xFFFFAA00; }
        cc.quark.util.RenderUtil.drawCustomText(ctx, weather, 2, 12, color);
    }
}
