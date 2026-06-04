package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.HungerManager;

/**
 * SaturationBar - Shows the player's food saturation level as a horizontal bar on the HUD.
 */
public class SaturationBar extends Module {

    private final IntSetting x = register(new IntSetting("X", "HUD X position", 100, 0, 4000));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 10,  0, 4000));

    public SaturationBar() {
        super("SaturationBar", "Shows saturation level on HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        HungerManager hunger = mc.player.getHungerManager();
        float saturation = hunger.getSaturationLevel();
        float maxSat = 20.0f;
        float pct = Math.max(0, Math.min(1, saturation / maxSat));

        int barW = 80;
        int barH = 6;
        int px = x.get();
        int py = y.get();

        // Background
        ctx.fill(px, py, px + barW, py + barH, 0xAA333333);
        // Fill
        int fillColor = saturation > 10 ? 0xFFFFAA00 : 0xFFFF5555;
        ctx.fill(px, py, px + (int)(barW * pct), py + barH, fillColor);
        // Border
        ctx.fill(px, py, px + barW, py + 1, 0xFF888888);
        ctx.fill(px, py + barH - 1, px + barW, py + barH, 0xFF888888);
        ctx.fill(px, py, px + 1, py + barH, 0xFF888888);
        ctx.fill(px + barW - 1, py, px + barW, py + barH, 0xFF888888);
        // Label
        ctx.drawTextWithShadow(mc.textRenderer,
                String.format("Sat: %.1f", saturation),
                px + barW + 3, py, 0xFFFFAA00);
    }
}
