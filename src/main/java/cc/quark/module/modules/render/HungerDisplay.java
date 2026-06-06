package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

/**
 * HungerDisplay - Detailed hunger and saturation HUD with colour-coded indicators.
 *
 * Shows food level (0-20) and saturation with colour coding:
 *   - Green  : value >= 15
 *   - Yellow : value >= 8
 *   - Red    : value <  8
 * Optionally renders a narrow progress bar for each value.
 */
public class HungerDisplay extends Module {

    private final IntSetting  posX        = register(new IntSetting ("X",          "HUD X position",               5,    0, 3840));
    private final IntSetting  posY        = register(new IntSetting ("Y",          "HUD Y position",               110,  0, 2160));
    private final BoolSetting showBar     = register(new BoolSetting("Bar",        "Show progress bars",           true));
    private final BoolSetting showSat     = register(new BoolSetting("Saturation", "Show saturation level",        true));
    private final BoolSetting showExhaust = register(new BoolSetting("Exhaustion", "Show exhaustion level",        false));

    public HungerDisplay() {
        super("HungerDisplay", "Shows hunger and saturation on HUD with colour coding", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.getWindow() == null) return;

        DrawContext ctx = event.getDrawContext();
        int x    = posX.get();
        int y    = posY.get();
        int lineH = mc.textRenderer.fontHeight + 2;
        int barW  = 60;
        int barH  = 3;

        // --- Food level ---
        int food = mc.player.getHungerManager().getFoodLevel();
        int foodColor = food >= 15 ? 0xFF55FF55 : food >= 8 ? 0xFFFFFF55 : 0xFFFF5555;

        String foodLabel = "Hunger: " + food + " / 20";
        ctx.drawTextWithShadow(mc.textRenderer, foodLabel, x, y, foodColor);
        y += lineH;

        if (showBar.isEnabled()) {
            int filled = (int)((food / 20.0f) * barW);
            ctx.fill(x, y, x + barW,     y + barH, 0xFF333333);
            ctx.fill(x, y, x + filled,   y + barH, foodColor);
            y += barH + 3;
        }

        // --- Saturation ---
        if (showSat.isEnabled()) {
            float sat = mc.player.getHungerManager().getSaturationLevel();
            int satColor = sat >= 15f ? 0xFF55FF55 : sat >= 8f ? 0xFFFFFF55 : 0xFFFF5555;
            String satLabel = "Saturation: " + String.format("%.1f", sat);
            ctx.drawTextWithShadow(mc.textRenderer, satLabel, x, y, satColor);
            y += lineH;

            if (showBar.isEnabled()) {
                float maxSat = 20.0f;
                int filled = (int)((Math.min(sat, maxSat) / maxSat) * barW);
                ctx.fill(x, y, x + barW,   y + barH, 0xFF333333);
                ctx.fill(x, y, x + filled, y + barH, satColor);
                y += barH + 3;
            }
        }

        // --- Exhaustion ---
        if (showExhaust.isEnabled()) {
            float exhaust = mc.player.getHungerManager().getExhaustion();
            // Exhaustion cycles 0-4 before consuming saturation
            int exhaustColor = exhaust < 2f ? 0xFF55FF55 : exhaust < 3f ? 0xFFFFFF55 : 0xFFFF5555;
            String exLabel = "Exhaust: " + String.format("%.2f", exhaust);
            ctx.drawTextWithShadow(mc.textRenderer, exLabel, x, y, exhaustColor);
        }
    }
}
