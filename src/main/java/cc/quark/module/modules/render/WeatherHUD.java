package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class WeatherHUD extends Module {

    private final BoolSetting enabled = register(new BoolSetting("Enabled", "Show the weather HUD element", true));
    private final IntSetting x = register(new IntSetting("X", "HUD X position", 4, 0, 3840));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 44, 0, 2160));

    public WeatherHUD() {
        super("WeatherHUD", "Shows current weather (clear / rain / thunder) with an icon", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!enabled.isEnabled()) return;
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();

        boolean thunder = mc.world.isThundering();
        boolean rain    = mc.world.isRaining();

        String icon;
        String label;
        int textColor;

        if (thunder) {
            icon      = "⚡";
            label     = "Thunder";
            textColor = 0xFFFFFF55;
        } else if (rain) {
            icon      = "~";
            label     = "Rain";
            textColor = 0xFF55AAFF;
        } else {
            icon      = "*";
            label     = "Clear";
            textColor = 0xFF55FF55;
        }

        int px = x.get(), py = y.get();
        ctx.drawTextWithShadow(mc.textRenderer, icon + " " + label, px, py, textColor);
    }
}
