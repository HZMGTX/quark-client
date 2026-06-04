package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.world.border.WorldBorder;

/**
 * WorldBorderESP - Draws a HUD overlay showing world border distance and
 * optionally fades the screen edge color based on proximity.
 */
public class WorldBorderESP extends Module {

    private final ColorSetting color = register(new ColorSetting(
            "Color", "Border indicator color", 0xFFFF5555));
    private final BoolSetting fade = register(new BoolSetting(
            "Fade", "Fade in screen overlay near border", true));

    public WorldBorderESP() {
        super("WorldBorderESP", "Shows world border as colored overlay", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();

        WorldBorder border = mc.world.getWorldBorder();
        double dist = border.getDistanceInsideBorder(mc.player);
        double size = border.getSize();

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        // HUD text
        int c = color.get();
        String text = String.format("Border: %.1fm | Size: %.0f", dist, size);
        ctx.drawTextWithShadow(mc.textRenderer, text, sw / 2 - mc.textRenderer.getWidth(text) / 2, 2, c);

        // Proximity fade overlay
        if (fade.isEnabled() && dist < 64) {
            float alpha = (float) Math.max(0, Math.min(1, 1.0 - dist / 64.0)) * 0.6f;
            int aInt = (int) (alpha * 255);
            int fadeColor = (aInt << 24) | (c & 0x00FFFFFF);
            // Draw border edges
            int thickness = Math.max(1, (int) (alpha * 12));
            ctx.fill(0, 0, thickness, sh, fadeColor);        // left
            ctx.fill(sw - thickness, 0, sw, sh, fadeColor);  // right
            ctx.fill(0, 0, sw, thickness, fadeColor);         // top
            ctx.fill(0, sh - thickness, sw, sh, fadeColor);   // bottom
        }
    }
}
