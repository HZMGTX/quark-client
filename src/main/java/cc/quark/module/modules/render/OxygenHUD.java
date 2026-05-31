package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class OxygenHUD extends Module {

    public OxygenHUD() {
        super("OxygenHUD", "Displays remaining oxygen/air bubbles; only visible when underwater", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        // Only render when the player is submerged
        if (!mc.player.isSubmergedInWater()) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        int air = mc.player.getAir();        // 0 – 300
        int maxAir = mc.player.getMaxAir();  // 300
        float pct = Math.max(0f, (float) air / maxAir);

        int barW = 82;
        int barH = 5;
        int x = sw / 2 - barW - 9;
        int y = sh - 49 + 10;

        ctx.fill(x, y, x + barW, y + barH, 0xFF333333);
        int fillW = (int) (barW * pct);
        ctx.fill(x, y, x + fillW, y + barH, pct > 0.4f ? 0xFF44AAFF : 0xFFFF4444);

        String label = String.format("Air: %d/%d", air, maxAir);
        RenderUtil.drawCustomText(ctx, label, x, y - 9, 0xFF88CCFF);
    }
}
