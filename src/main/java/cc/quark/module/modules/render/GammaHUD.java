package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

/**
 * GammaHUD - Displays the current gamma level on the HUD with color-coded feedback.
 *
 * Green  = gamma at or above 1.0 (full-bright territory)
 * Yellow = 0.5 - 1.0
 * Red    = below 0.5 (dark)
 */
public class GammaHUD extends Module {

    private final IntSetting x = register(new IntSetting("X", "HUD X position", 2, 0, 1000));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 14, 0, 600));

    private final BoolSetting showBar = register(new BoolSetting(
            "Show Bar", "Draw a small progress bar below the text", true));

    public GammaHUD() {
        super("GammaHUD", "Displays current gamma level on the HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.options == null) return;
        DrawContext ctx = e.getDrawContext();

        double gamma = mc.options.getGamma().getValue();
        int px = x.get();
        int py = y.get();

        int textColor = gamma >= 1.0 ? 0xFF55FF55 : gamma >= 0.5 ? 0xFFFFFF55 : 0xFFFF5555;
        String label = String.format("Gamma: %.1f", gamma);
        ctx.drawTextWithShadow(mc.textRenderer, label, px, py, textColor);

        if (showBar.isEnabled()) {
            int barW = 50;
            double clamped = Math.min(gamma, 2.0);
            int filled = (int)(clamped / 2.0 * barW);
            ctx.fill(px, py + 10, px + barW, py + 13, 0xAA222222);
            ctx.fill(px, py + 10, px + filled, py + 13, textColor);
        }
    }
}
