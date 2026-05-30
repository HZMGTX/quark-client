package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.gui.DrawContext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClockHUD extends Module {

    private final IntSetting posX = register(new IntSetting(
            "X", "HUD X position", 4, 0, 500));
    private final IntSetting posY = register(new IntSetting(
            "Y", "HUD Y position", 4, 0, 500));
    private final ModeSetting format = register(new ModeSetting(
            "Format", "Time display format", "24h", "24h", "12h"));
    private final ColorSetting color = register(new ColorSetting(
            "Color", "Text color (ARGB)", 0xFFFFFFFF));

    private static final DateTimeFormatter FMT_24H = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FMT_12H = DateTimeFormatter.ofPattern("h:mm a");

    public ClockHUD() {
        super("ClockHUD", "Displays real-world time as a HUD overlay", Category.MISC);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        DrawContext ctx = event.getDrawContext();
        DateTimeFormatter fmt = format.is("12h") ? FMT_12H : FMT_24H;
        String text = LocalTime.now().format(fmt);
        ctx.drawTextWithShadow(mc.textRenderer, text, posX.get(), posY.get(), color.get());
    }
}
