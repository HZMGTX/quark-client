package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class LevelDisplay extends Module {

    private final IntSetting  posX      = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting  posY      = register(new IntSetting("Y", "HUD Y position", 4, 0, 3000));
    private final IntSetting  barWidth  = register(new IntSetting("Bar Width", "Width of the XP progress bar in pixels", 80, 20, 300));
    private final IntSetting  barHeight = register(new IntSetting("Bar Height", "Height of the XP progress bar", 4, 2, 16));
    private final BoolSetting showBar   = register(new BoolSetting("Show Bar", "Draw XP progress bar", true));
    private final BoolSetting showPct   = register(new BoolSetting("Show Percent", "Show progress as percentage", true));
    private final BoolSetting showTotal = register(new BoolSetting("Show Total XP", "Show total XP points", false));

    public LevelDisplay() {
        super("LevelDisplay", "Shows XP level and progress bar on HUD", Category.RENDER);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int x = posX.get(), y = posY.get();
        int level = mc.player.experienceLevel;
        float progress = mc.player.experienceProgress;
        int lh = mc.textRenderer.fontHeight + 2;

        // Level label
        String levelText = showPct.isEnabled()
                ? String.format("§aLevel %d §7(%.1f%%)", level, progress * 100f)
                : "§aLevel " + level;
        ctx.drawTextWithShadow(mc.textRenderer, levelText, x, y, 0xFF55FF55);
        y += lh;

        // XP bar
        if (showBar.isEnabled()) {
            int bw = barWidth.get();
            int bh = barHeight.get();
            int filled = (int) (bw * progress);
            ctx.fill(x, y, x + bw, y + bh, 0xFF222222);
            if (filled > 0) ctx.fill(x, y, x + filled, y + bh, 0xFF55FF55);
            ctx.fill(x, y, x + bw, y, 0xFF444444);           // top border
            ctx.fill(x, y + bh, x + bw, y + bh, 0xFF444444); // bottom border
            y += bh + 2;
        }

        // Total XP
        if (showTotal.isEnabled()) {
            int total = getTotalXP(level, progress);
            ctx.drawTextWithShadow(mc.textRenderer,
                    String.format("§7Total XP: §f%d", total), x, y, 0xFFAAAAAA);
        }
    }

    /** Approximates total XP based on level and progress. */
    private int getTotalXP(int level, float progress) {
        int base;
        if (level < 16) {
            base = level * level + 6 * level;
        } else if (level < 31) {
            base = (int)(2.5 * level * level - 40.5 * level + 360);
        } else {
            base = (int)(4.5 * level * level - 162.5 * level + 2220);
        }
        int toNext = level < 16 ? (2 * level + 7)
                   : level < 31 ? (5 * level - 38)
                   : (9 * level - 158);
        return base + (int)(toNext * progress);
    }
}
