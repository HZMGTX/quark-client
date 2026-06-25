package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

/**
 * TimeHUD — displays an in-game time overlay showing the current Minecraft
 * world time in hours:minutes format, with optional real-world clock and
 * day counter.
 */
public class TimeHUD extends Module {

    private final IntSetting xPos = register(new IntSetting(
            "X", "Horizontal position", 5, 0, 3000));

    private final IntSetting yPos = register(new IntSetting(
            "Y", "Vertical position (from bottom)", 50, 0, 3000));

    private final ModeSetting timeFormat = register(new ModeSetting(
            "Format", "Time format to display", "12h", "12h", "24h", "Both"));

    private final BoolSetting showDay = register(new BoolSetting(
            "Show Day", "Show current in-game day number", true));

    private final BoolSetting showRealTime = register(new BoolSetting(
            "Real Time", "Also show your real-world system time", false));

    private final BoolSetting showBackground = register(new BoolSetting(
            "Background", "Draw a dark background behind the text", true));

    private final BoolSetting showPhase = register(new BoolSetting(
            "Phase", "Show day/night phase label (Dawn, Day, Dusk, Night)", true));

    public TimeHUD() {
        super("TimeHUD", "Shows in-game time overlay", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null) return;

        // Minecraft world time: 0 = 6:00 AM, 6000 = noon, 12000 = 6 PM, 18000 = midnight
        long worldTime = mc.world.getTimeOfDay() % 24000L;

        // Convert to hours and minutes (Minecraft: 24000 ticks = 24 hours)
        double totalHours = (worldTime / 1000.0 + 6.0) % 24.0;
        int hours   = (int) totalHours;
        int minutes = (int)((totalHours - hours) * 60);

        String ingameTime;
        if (timeFormat.get().equals("12h")) {
            String ampm = hours >= 12 ? "PM" : "AM";
            int h12 = hours % 12;
            if (h12 == 0) h12 = 12;
            ingameTime = String.format("%d:%02d %s", h12, minutes, ampm);
        } else {
            ingameTime = String.format("%02d:%02d", hours, minutes);
        }

        String phase = getPhase(worldTime);
        long day = mc.world.getTimeOfDay() / 24000L + 1;

        DrawContext ctx = event.getDrawContext();
        int screenH = mc.getWindow().getScaledHeight();
        int x = xPos.get();
        int y = screenH - yPos.get();
        int lineH = mc.textRenderer.fontHeight + 2;

        java.util.List<String> lines = new java.util.ArrayList<>();
        lines.add("§e" + ingameTime);
        if (timeFormat.get().equals("Both")) {
            lines.add(String.format("§7%02d:%02d", hours, minutes));
        }
        if (showPhase.isEnabled()) lines.add("§7" + phase);
        if (showDay.isEnabled()) lines.add("§7Day: §f" + day);
        if (showRealTime.isEnabled()) {
            java.time.LocalTime now = java.time.LocalTime.now();
            lines.add("§b" + String.format("%02d:%02d:%02d",
                    now.getHour(), now.getMinute(), now.getSecond()));
        }

        // Background
        if (showBackground.isEnabled()) {
            int maxW = lines.stream()
                    .mapToInt(l -> mc.textRenderer.getWidth(net.minecraft.text.Text.of(l)))
                    .max().orElse(0);
            ctx.fill(x - 3, y - 2, x + maxW + 3, y + lines.size() * lineH + 1, 0xAA111111);
        }

        for (String line : lines) {
            ctx.drawTextWithShadow(mc.textRenderer, net.minecraft.text.Text.of(line), x, y, 0xFFFFFFFF);
            y += lineH;
        }
    }

    private String getPhase(long worldTime) {
        if (worldTime < 1000)  return "Dawn";
        if (worldTime < 12000) return "Day";
        if (worldTime < 13000) return "Dusk";
        if (worldTime < 23000) return "Night";
        return "Dawn";
    }
}
