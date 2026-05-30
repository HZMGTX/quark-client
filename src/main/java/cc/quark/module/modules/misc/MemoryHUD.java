package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class MemoryHUD extends Module {

    private final IntSetting posX = register(new IntSetting(
            "X", "HUD X position", 4, 0, 500));
    private final IntSetting posY = register(new IntSetting(
            "Y", "HUD Y position", 14, 0, 500));

    public MemoryHUD() {
        super("MemoryHUD", "Displays heap memory usage as a HUD overlay", Category.MISC);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long maxMb = rt.maxMemory() / (1024 * 1024);
        int pct = maxMb > 0 ? (int) (usedMb * 100L / maxMb) : 0;

        int color;
        if (pct < 70) {
            color = 0xFF55FF55; // green
        } else if (pct < 90) {
            color = 0xFFFFFF55; // yellow
        } else {
            color = 0xFFFF5555; // red
        }

        String text = "RAM: " + usedMb + "mb / " + maxMb + "mb (" + pct + "%)";
        DrawContext ctx = event.getDrawContext();
        ctx.drawTextWithShadow(mc.textRenderer, text, posX.get(), posY.get(), color);
    }
}
