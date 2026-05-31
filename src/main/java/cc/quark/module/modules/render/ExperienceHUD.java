package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class ExperienceHUD extends Module {

    public ExperienceHUD() {
        super("ExperienceHUD", "Shows current XP level and progress as a numeric HUD element", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        int level = mc.player.experienceLevel;
        float progress = mc.player.experienceProgress;

        String text = String.format("XP %d  (%.0f%%)", level, progress * 100f);
        int x = sw / 2 - mc.textRenderer.getWidth(text) / 2;
        int y = sh - 35;
        RenderUtil.drawCustomText(ctx, text, x, y, 0xFF80FF20);
    }
}
