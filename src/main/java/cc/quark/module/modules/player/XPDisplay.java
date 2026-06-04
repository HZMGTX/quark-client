package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class XPDisplay extends Module {

    private final IntSetting x = register(new IntSetting(
            "X", "HUD X position", 4, 0, 1920));
    private final IntSetting y = register(new IntSetting(
            "Y", "HUD Y position", 160, 0, 1080));

    public XPDisplay() {
        super("XPDisplay", "Shows XP level and progress on HUD", Category.PLAYER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        int level = mc.player.experienceLevel;
        float progress = mc.player.experienceProgress;
        int progressPct = (int) (progress * 100);

        String text = "XP: " + level + " (" + progressPct + "%)";
        ctx.drawTextWithShadow(mc.textRenderer, text, x.get(), y.get(), 0xFF55FF55);

        // Draw XP bar
        int barWidth = 80;
        int barHeight = 4;
        int bx = x.get();
        int by = y.get() + 10;
        // Background
        ctx.fill(bx, by, bx + barWidth, by + barHeight, 0xFF333333);
        // Fill
        int fillWidth = (int) (barWidth * progress);
        ctx.fill(bx, by, bx + fillWidth, by + barHeight, 0xFF55FF55);
    }
}
