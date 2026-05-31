package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.network.BossBar;

import java.util.Collection;
import java.util.UUID;

/**
 * BossBarESP — enhances the boss bar display with health percentage text and a
 * custom styled colored bar. Reads boss bar data directly from the BossBarHud
 * client-side storage via reflection, since the field is private.
 */
public class BossBarESP extends Module {

    private final BoolSetting showPercent = register(new BoolSetting(
            "Percent", "Show health percentage on boss bar", true));

    private final BoolSetting showName = register(new BoolSetting(
            "Name", "Show boss name above bar", true));

    private final ModeSetting style = register(new ModeSetting(
            "Style", "Bar visual style", "Gradient", "Gradient", "Solid", "Rainbow"));

    private final IntSetting barHeight = register(new IntSetting(
            "Bar Height", "Height in pixels of the custom bar", 6, 2, 16));

    private final DoubleSetting bgAlphaSetting = register(new DoubleSetting(
            "Alpha", "Bar background alpha (0-1)", 0.6, 0.1, 1.0));

    /** Reflective access to the private bossBars field in BossBarHud. */
    private static java.lang.reflect.Field bossBarField = null;

    static {
        try {
            for (java.lang.reflect.Field f : BossBarHud.class.getDeclaredFields()) {
                if (java.util.Map.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    bossBarField = f;
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    public BossBarESP() {
        super("BossBarESP", "Enhances boss bar display with extra info", Category.RENDER);
    }

    @SuppressWarnings("unchecked")
    private Collection<BossBar> getBossBars() {
        if (mc.inGameHud == null || bossBarField == null) return java.util.Collections.emptyList();
        try {
            java.util.Map<UUID, BossBar> map =
                    (java.util.Map<UUID, BossBar>) bossBarField.get(mc.inGameHud.getBossBarHud());
            return map != null ? map.values() : java.util.Collections.emptyList();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        Collection<BossBar> bars = getBossBars();
        if (bars.isEmpty() || mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int screenW = mc.getWindow().getScaledWidth();
        int barW = Math.min(182, screenW - 40);
        int x = screenW / 2 - barW / 2;
        int startY = 12;
        int barH = barHeight.get();
        int lineSpacing = barH + mc.textRenderer.fontHeight + 10;
        int bgAlpha = (int)(bgAlphaSetting.get() * 255);

        int idx = 0;
        for (BossBar bar : bars) {
            float pct = bar.getPercent();
            int y = startY + idx * lineSpacing;
            idx++;

            // Background
            ctx.fill(x - 1, y - 1, x + barW + 1, y + barH + 1, (bgAlpha << 24));

            // Filled portion
            int fillW = (int)(barW * pct);
            int fillColor = resolveColor(pct);

            switch (style.get()) {
                case "Gradient" ->
                    RenderUtil.drawGradientRect(ctx, x, y, x + fillW, y + barH, fillColor, darken(fillColor, 0.55f));
                case "Rainbow" -> {
                    float hue = (System.currentTimeMillis() % 2000L) / 2000.0f + idx * 0.18f;
                    int rc = java.awt.Color.HSBtoRGB(hue % 1.0f, 1.0f, 1.0f) | 0xFF000000;
                    RenderUtil.drawGradientRect(ctx, x, y, x + fillW, y + barH, rc, darken(rc, 0.6f));
                }
                default -> ctx.fill(x, y, x + fillW, y + barH, fillColor);
            }

            // Name label above bar
            if (showName.isEnabled()) {
                String name = bar.getName().getString();
                int nameW = mc.textRenderer.getWidth(name);
                RenderUtil.drawCustomText(ctx, name,
                        x + barW / 2 - nameW / 2, y - mc.textRenderer.fontHeight - 2, 0xFFFFFFFF);
            }

            // Percentage label centred on bar
            if (showPercent.isEnabled()) {
                String pctStr = String.format("%.1f%%", pct * 100f);
                int pctW = mc.textRenderer.getWidth(pctStr);
                RenderUtil.drawCustomText(ctx, pctStr,
                        x + barW / 2 - pctW / 2,
                        y + barH / 2 - mc.textRenderer.fontHeight / 2,
                        0xFFFFFFFF);
            }
        }
    }

    private int resolveColor(float pct) {
        if (pct > 0.6f) return 0xFF44FF88;
        if (pct > 0.3f) return 0xFFFFBB00;
        return 0xFFFF3333;
    }

    private int darken(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int)(((color >> 16) & 0xFF) * factor);
        int g = (int)(((color >> 8) & 0xFF) * factor);
        int b = (int)((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
