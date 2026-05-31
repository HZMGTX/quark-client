package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.gui.DrawContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeDisplay extends Module {

    private final IntSetting   posX     = register(new IntSetting("X",       "HUD X position", 4, 0, 500));
    private final IntSetting   posY     = register(new IntSetting("Y",       "HUD Y position", 20, 0, 500));
    private final ModeSetting  format   = register(new ModeSetting("Format",  "Time format",    "24h", "24h", "12h"));
    private final BoolSetting  showDate = register(new BoolSetting("ShowDate", "Show today's date below the time", false));
    private final ColorSetting color    = register(new ColorSetting("Color",  "Text color",     0xFFFFFFFF));

    private static final DateTimeFormatter FMT_24H      = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FMT_12H      = DateTimeFormatter.ofPattern("h:mm:ss a");
    private static final DateTimeFormatter FMT_DATE     = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public TimeDisplay() {
        super("TimeDisplay", "Shows real-world time as a HUD element with optional date display", Category.MISC);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        DrawContext ctx = event.getDrawContext();
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter timeFmt = format.get().equals("12h") ? FMT_12H : FMT_24H;
        String timeStr = now.format(timeFmt);
        int lh = mc.textRenderer.fontHeight + 2;

        ctx.drawTextWithShadow(mc.textRenderer, timeStr, posX.get(), posY.get(), color.get());
        if (showDate.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, now.format(FMT_DATE), posX.get(), posY.get() + lh, color.get());
        }
    }
}
