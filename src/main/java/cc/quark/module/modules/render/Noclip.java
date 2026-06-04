package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import net.minecraft.client.gui.DrawContext;

/**
 * Noclip (Render) - visualizes the noclip status with an on-screen overlay.
 * This is a client-side HUD indicator module; actual noclip movement logic
 * would be handled separately via movement mixins.
 */
public class Noclip extends Module {

    private final ColorSetting color = register(new ColorSetting(
            "Color", "Overlay color (ARGB)", 0x55FFFFFF));

    private final BoolSetting wireframe = register(new BoolSetting(
            "Wireframe", "Show wireframe crosshair indicator", true));

    public Noclip() {
        super("Noclip", "Client-side noclip visualization", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int argb = color.get();

        // Draw semi-transparent overlay to indicate noclip is active
        if (wireframe.isEnabled()) {
            // Crosshair-style indicator in corners
            int margin = 8;
            int size = 16;

            // Top-left
            ctx.fill(margin, margin, margin + size, margin + 1, argb);
            ctx.fill(margin, margin, margin + 1, margin + size, argb);
            // Top-right
            ctx.fill(sw - margin - size, margin, sw - margin, margin + 1, argb);
            ctx.fill(sw - margin - 1, margin, sw - margin, margin + size, argb);
            // Bottom-left
            ctx.fill(margin, sh - margin - 1, margin + size, sh - margin, argb);
            ctx.fill(margin, sh - margin - size, margin + 1, sh - margin, argb);
            // Bottom-right
            ctx.fill(sw - margin - size, sh - margin - 1, sw - margin, sh - margin, argb);
            ctx.fill(sw - margin - 1, sh - margin - size, sw - margin, sh - margin, argb);
        }

        // Draw "NOCLIP" label at top
        String label = "§cNOCLIP";
        int tw = mc.textRenderer.getWidth(label);
        ctx.drawTextWithShadow(mc.textRenderer, label, (sw - tw) / 2, 4, argb);
    }
}
