package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.Deque;

public class FpsDisplay extends Module {

    private final ModeSetting style  = register(new ModeSetting("Style", "Display style", "Simple", "Simple", "Bar", "Graph"));
    private final IntSetting  posX   = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting  posY   = register(new IntSetting("Y", "HUD Y position", 4, 0, 3000));
    private final BoolSetting label  = register(new BoolSetting("Label", "Show FPS label prefix", true));

    private final Deque<Integer> history = new ArrayDeque<>();
    private static final int GRAPH_W = 60;
    private static final int GRAPH_H = 20;

    public FpsDisplay() {
        super("FpsDisplay", "Shows the current frames per second with color-coded display", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.getWindow() == null) return;
        DrawContext ctx = event.getDrawContext();
        int fps = mc.getCurrentFps();
        int x = posX.get(), y = posY.get();

        history.addLast(fps);
        while (history.size() > GRAPH_W) history.pollFirst();

        int fpsColor = fps >= 60 ? 0xFF55FF55 : fps >= 30 ? 0xFFFFFF55 : 0xFFFF5555;

        switch (style.get()) {
            case "Simple" -> {
                String text = label.isEnabled() ? "FPS: " + fps : String.valueOf(fps);
                ctx.drawTextWithShadow(mc.textRenderer, text, x, y, fpsColor);
            }
            case "Bar" -> {
                String text = label.isEnabled() ? "FPS: " + fps : String.valueOf(fps);
                ctx.drawTextWithShadow(mc.textRenderer, text, x, y, fpsColor);
                int barW = 60;
                int filled = Math.min(barW, fps * barW / 120);
                ctx.fill(x, y + 10, x + barW, y + 13, 0xAA222222);
                ctx.fill(x, y + 10, x + filled, y + 13, fpsColor);
            }
            case "Graph" -> {
                String text = label.isEnabled() ? "FPS: " + fps : String.valueOf(fps);
                ctx.drawTextWithShadow(mc.textRenderer, text, x, y, fpsColor);
                ctx.fill(x, y + 10, x + GRAPH_W, y + 10 + GRAPH_H, 0xAA111111);
                Integer[] arr = history.toArray(new Integer[0]);
                int maxVal = 1;
                for (int v : arr) if (v > maxVal) maxVal = v;
                for (int i = 0; i < arr.length - 1; i++) {
                    int h1 = arr[i] * GRAPH_H / maxVal;
                    int h2 = arr[i + 1] * GRAPH_H / maxVal;
                    int gx = x + i;
                    int gy1 = y + 10 + GRAPH_H - h1;
                    int gy2 = y + 10 + GRAPH_H - h2;
                    int col = arr[i] >= 60 ? 0xFF55FF55 : arr[i] >= 30 ? 0xFFFFFF55 : 0xFFFF5555;
                    ctx.fill(gx, Math.min(gy1, gy2), gx + 1, Math.max(gy1, gy2) + 1, col);
                }
            }
        }
    }
}
