package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

/**
 * OxygenBar - Displays the player's oxygen/air supply as a custom HUD bar.
 */
public class OxygenBar extends Module {

    private final IntSetting x = register(new IntSetting("X", "HUD X position", 100, 0, 4000));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 20,  0, 4000));

    public OxygenBar() {
        super("OxygenBar", "Shows oxygen level as custom HUD bar", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        int air    = mc.player.getAir();
        int maxAir = mc.player.getMaxAir();
        float pct  = Math.max(0f, Math.min(1f, (float) air / maxAir));

        // Only show when underwater or low on air
        if (air >= maxAir && mc.player.isSubmergedInWater() == false) return;

        int barW = 80;
        int barH = 6;
        int px = x.get();
        int py = y.get();

        // Background
        ctx.fill(px, py, px + barW, py + barH, 0xAA333333);
        // Fill color: blue when fine, red when almost out
        int fillColor = pct > 0.4f ? 0xFF5555FF : 0xFFFF5555;
        ctx.fill(px, py, px + (int)(barW * pct), py + barH, fillColor);
        // Border
        ctx.fill(px, py, px + barW, py + 1, 0xFF888888);
        ctx.fill(px, py + barH - 1, px + barW, py + barH, 0xFF888888);
        ctx.fill(px, py, px + 1, py + barH, 0xFF888888);
        ctx.fill(px + barW - 1, py, px + barW, py + barH, 0xFF888888);
        // Label
        ctx.drawTextWithShadow(mc.textRenderer,
                String.format("O2: %d%%", (int)(pct * 100)),
                px + barW + 3, py, 0xFF5555FF);
    }
}
