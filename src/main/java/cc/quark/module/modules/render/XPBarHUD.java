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
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * XPBarHUD — shows an enhanced XP bar with the player's current level, the
 * amount of XP needed to reach the next level, and the total progress
 * fraction, rendered as a custom styled overlay.
 */
public class XPBarHUD extends Module {

    private final IntSetting xPos = register(new IntSetting(
            "X", "Horizontal position", 5, 0, 3000));

    private final IntSetting yPos = register(new IntSetting(
            "Y", "Vertical position (from bottom)", 30, 0, 3000));

    private final IntSetting barWidth = register(new IntSetting(
            "Bar Width", "Width of the XP bar in pixels", 180, 40, 400));

    private final IntSetting barHeight = register(new IntSetting(
            "Bar Height", "Height of the XP bar in pixels", 6, 2, 16));

    private final ModeSetting style = register(new ModeSetting(
            "Style", "Bar color style", "Green", "Green", "Rainbow", "Level"));

    private final BoolSetting showLevel = register(new BoolSetting(
            "Show Level", "Display level number on the bar", true));

    private final BoolSetting showNeeded = register(new BoolSetting(
            "Show Needed XP", "Show how much XP is needed for next level", true));

    private final BoolSetting shadow = register(new BoolSetting(
            "Shadow", "Draw background shadow behind the bar", true));

    public XPBarHUD() {
        super("XPBarHUD", "Enhanced XP bar display with level info", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        ClientPlayerEntity player = mc.player;
        float xpProgress = player.experienceProgress; // 0.0 to 1.0
        int level = player.experienceLevel;
        int xpForNextLevel = getXpForLevel(level);

        DrawContext ctx = event.getDrawContext();
        int screenH = mc.getWindow().getScaledHeight();

        int x = xPos.get();
        int y = screenH - yPos.get();
        int bw = barWidth.get();
        int bh = barHeight.get();

        // Background
        if (shadow.isEnabled()) {
            ctx.fill(x - 1, y - 1, x + bw + 1, y + bh + 1, 0xAA000000);
        }

        // Empty bar
        ctx.fill(x, y, x + bw, y + bh, 0x55333333);

        // Filled portion
        int fillW = (int) (bw * xpProgress);
        int fillColor = resolveFillColor(level, xpProgress);

        if (style.get().equals("Rainbow")) {
            RenderUtil.drawGradientRect(ctx, x, y, x + fillW, y + bh,
                    fillColor, rotateHue(fillColor, 0.15f));
        } else {
            RenderUtil.drawGradientRect(ctx, x, y, x + fillW, y + bh,
                    fillColor, darken(fillColor, 0.6f));
        }

        // Level text on bar center
        if (showLevel.isEnabled()) {
            String levelStr = "Lv." + level;
            int tw = mc.textRenderer.getWidth(levelStr);
            RenderUtil.drawCustomText(ctx, levelStr,
                    x + bw / 2 - tw / 2,
                    y + bh / 2 - mc.textRenderer.fontHeight / 2,
                    0xFFFFFFFF);
        }

        // XP needed text below bar
        if (showNeeded.isEnabled()) {
            int xpHave = (int) (xpForNextLevel * xpProgress);
            String needStr = xpHave + " / " + xpForNextLevel + " XP";
            RenderUtil.drawCustomText(ctx, needStr, x, y + bh + 3, 0xFFAAAAAA);
        }
    }

    private int resolveFillColor(int level, float pct) {
        if (style.get().equals("Rainbow")) {
            float hue = (System.currentTimeMillis() % 2000L) / 2000.0f;
            return java.awt.Color.HSBtoRGB(hue % 1.0f, 1.0f, 1.0f) | 0xFF000000;
        }
        if (style.get().equals("Level")) {
            if (level < 16) return 0xFF55FF55;
            if (level < 31) return 0xFFFFFF55;
            return 0xFFFF5555;
        }
        return 0xFF55DD00; // Green default
    }

    private int rotateHue(int color, float offset) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float[] hsb = java.awt.Color.RGBtoHSB((int)(r*255), (int)(g*255), (int)(b*255), null);
        hsb[0] = (hsb[0] + offset) % 1.0f;
        return java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]) | 0xFF000000;
    }

    private int darken(int color, float f) {
        int a = (color >> 24) & 0xFF;
        int r = (int)(((color >> 16) & 0xFF) * f);
        int g = (int)(((color >> 8) & 0xFF) * f);
        int b = (int)((color & 0xFF) * f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Returns the XP required to go from level {@code level} to level+1.
     * Matches vanilla formula: 0-15 → 2l+7, 16-30 → 5l-38, 31+ → 9l-158
     */
    private int getXpForLevel(int level) {
        if (level <= 15) return 2 * level + 7;
        if (level <= 30) return 5 * level - 38;
        return 9 * level - 158;
    }
}
