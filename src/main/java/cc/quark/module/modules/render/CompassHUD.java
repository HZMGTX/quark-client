package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class CompassHUD extends Module {

    private final IntSetting width = register(new IntSetting("Width", "Width of the compass bar", 200, 50, 400));
    private final IntSetting yPos = register(new IntSetting("Y", "Y position of compass bar", 5, 0, 50));
    private final ColorSetting color = register(new ColorSetting("Color", "Default direction label color", 0xFFFFFFFF));
    private final ColorSetting highlightColor = register(new ColorSetting("HighlightColor", "Color of the currently-faced direction", 0xFFFF4444));

    private static final String[] DIRECTIONS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    private static final float[] DIR_ANGLES = {0, 45, 90, 135, 180, 225, 270, 315};

    public CompassHUD() {
        super("CompassHUD", "Draws a compass bar at the top of the screen showing cardinal directions", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int screenW = mc.getWindow().getScaledWidth();
        int barWidth = width.get();
        int startX = (screenW - barWidth) / 2;
        int barY = yPos.get();

        // Background bar
        ctx.fill(startX, barY, startX + barWidth, barY + 14, 0x88000000);

        float yaw = mc.player.getYaw() % 360;
        if (yaw < 0) yaw += 360;

        int defaultColor = color.get();
        int hlColor = highlightColor.get();

        // Draw each direction label based on player yaw
        for (int i = 0; i < DIRECTIONS.length; i++) {
            float dirAngle = DIR_ANGLES[i];
            // Difference between direction angle and player yaw
            float diff = dirAngle - yaw;
            // Normalize to -180..180
            while (diff > 180) diff -= 360;
            while (diff < -180) diff += 360;

            // Skip if outside visible range
            float visibleRange = 90.0f;
            if (Math.abs(diff) > visibleRange) continue;

            // Map diff to screen position
            float ratio = diff / visibleRange; // -1 to 1
            int dirX = startX + (int)((ratio + 1.0f) / 2.0f * barWidth);

            // Determine if this is roughly the current facing direction
            boolean isHighlighted = Math.abs(diff) < 15;
            int textColor = isHighlighted ? hlColor : defaultColor;

            String label = DIRECTIONS[i];
            int textW = mc.textRenderer.getWidth(label);
            ctx.drawTextWithShadow(mc.textRenderer, label, dirX - textW / 2, barY + 3, textColor);
        }

        // Draw center tick mark
        ctx.fill(startX + barWidth / 2 - 1, barY + 12, startX + barWidth / 2 + 1, barY + 14, hlColor);
    }
}
