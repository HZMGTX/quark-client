package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class WorldTimeClock extends Module {

    private final IntSetting x         = register(new IntSetting("X",     "HUD X position",                    4,   0, 1920));
    private final IntSetting y         = register(new IntSetting("Y",     "HUD Y position",                    140, 0, 1080));
    private final BoolSetting show24h  = register(new BoolSetting("24h",  "Display in 24-hour format",         true));

    public WorldTimeClock() {
        super("WorldTimeClock", "Shows in-game time as real clock", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        // MC day is 24000 ticks. Time 0 = 6:00 AM (dawn)
        long timeOfDay = mc.world.getTimeOfDay() % 24000L;
        // Convert to real hours: 0 ticks = 6:00, 6000 = 12:00, 12000 = 18:00, 18000 = 0:00
        double realHours = (timeOfDay / 24000.0) * 24.0 + 6.0;
        if (realHours >= 24.0) realHours -= 24.0;

        int hours   = (int) realHours;
        int minutes = (int)((realHours - hours) * 60);

        String timeStr;
        if (show24h.isEnabled()) {
            timeStr = String.format("%02d:%02d", hours, minutes);
        } else {
            String ampm = hours < 12 ? "AM" : "PM";
            int h12     = hours % 12;
            if (h12 == 0) h12 = 12;
            timeStr = String.format("%d:%02d %s", h12, minutes, ampm);
        }

        // Determine color by time of day
        int textColor;
        if (hours >= 6 && hours < 12)        textColor = 0xFFFFDD88; // Morning
        else if (hours >= 12 && hours < 18)  textColor = 0xFFFFFFFF; // Afternoon
        else if (hours >= 18 && hours < 20)  textColor = 0xFFFF8844; // Evening
        else                                  textColor = 0xFF5599FF; // Night

        ctx.drawTextWithShadow(mc.textRenderer, "Time: " + timeStr, x.get(), y.get(), textColor);
    }
}
