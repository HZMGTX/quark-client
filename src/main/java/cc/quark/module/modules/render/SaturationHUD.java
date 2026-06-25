package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.HungerManager;

public class SaturationHUD extends Module {

    public SaturationHUD() {
        super("SaturationHUD", "HUD bar showing current food saturation level", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        HungerManager hunger = mc.player.getHungerManager();
        float sat = Math.min(hunger.getSaturationLevel(), 20f);
        float pct = sat / 20f;

        int barW = 82;
        int barH = 5;
        // Positioned below the vanilla hunger bar (roughly)
        int x = sw / 2 + 9;
        int y = sh - 49 + 10;

        ctx.fill(x, y, x + barW, y + barH, 0xFF333333);
        int fillW = (int) (barW * pct);
        int barColor = sat > 10 ? 0xFFFFAA00 : (sat > 4 ? 0xFFFF6600 : 0xFFFF2200);
        ctx.fill(x, y, x + fillW, y + barH, barColor);

        String label = String.format("Sat: %.1f", sat);
        RenderUtil.drawCustomText(ctx, label, x, y - 9, 0xFFFFCC44);
    }
}
