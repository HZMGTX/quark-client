package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeDisplay extends Module {
    private final BoolSetting realTime = register(new BoolSetting("Real Time", "Show real-world clock", true));
    private final BoolSetting gameTime = register(new BoolSetting("Game Time", "Show in-game day time", true));
    private final IntSetting x = register(new IntSetting("X", "X position", 2, 0, 1000));
    private final IntSetting y = register(new IntSetting("Y", "Y position", 120, 0, 600));

    public TimeDisplay() { super("TimeDisplay", "Shows real-time and in-game time on HUD", Category.RENDER); }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        DrawContext ctx = e.getDrawContext();
        int py = y.get();
        if (realTime.isEnabled()) {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            cc.quark.util.RenderUtil.drawCustomText(ctx, "Time: " + time, x.get(), py, 0xFFFFAA00);
            py += 10;
        }
        if (gameTime.isEnabled() && mc.world != null) {
            long worldTime = mc.world.getTimeOfDay() % 24000;
            int hours = (int)(worldTime / 1000 + 6) % 24;
            int mins = (int)(worldTime % 1000 * 60 / 1000);
            cc.quark.util.RenderUtil.drawCustomText(ctx, String.format("Day: %02d:%02d", hours, mins), x.get(), py, 0xFFAAAAFF);
        }
    }
}
