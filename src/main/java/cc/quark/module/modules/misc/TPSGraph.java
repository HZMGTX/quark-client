package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.Deque;

public class TPSGraph extends Module {

    private final IntSetting historyTicks = register(new IntSetting(
            "History", "Number of TPS samples to display in graph", 60, 10, 200));

    private final Deque<Float> tpsHistory = new ArrayDeque<>();
    private long lastTickTime = System.currentTimeMillis();

    public TPSGraph() {
        super("TPSGraph", "Renders a TPS history graph in the corner of the screen", Category.MISC);
    }

    @Override
    public void onDisable() {
        tpsHistory.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        long now = System.currentTimeMillis();
        long delta = now - lastTickTime;
        lastTickTime = now;
        float tps = delta > 0 ? Math.min(20.0f, 1000.0f / delta) : 20.0f;
        tpsHistory.addLast(tps);
        while (tpsHistory.size() > historyTicks.get()) {
            tpsHistory.removeFirst();
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int w = mc.getWindow().getScaledWidth();
        int graphW = 80, graphH = 30;
        int x = w - graphW - 4, y = 4;

        ctx.fill(x - 1, y - 1, x + graphW + 1, y + graphH + 1, 0xAA000000);
        ctx.drawTextWithShadow(mc.textRenderer, "TPS", x, y, 0xFFAAAAAA);

        float[] samples = tpsHistory.stream().mapToDouble(Float::doubleValue)
                .collect(() -> new java.util.ArrayList<Double>(), java.util.ArrayList::add, java.util.ArrayList::addAll)
                .stream().map(Double::floatValue).collect(java.util.stream.Collectors.toList())
                .stream().mapToDouble(Float::doubleValue).toArray().length > 0
                ? new float[0] : new float[0];

        float[] arr = new float[tpsHistory.size()];
        int idx = 0;
        for (float f : tpsHistory) arr[idx++] = f;

        for (int i = 1; i < arr.length; i++) {
            int x1 = x + (i - 1) * graphW / Math.max(1, arr.length - 1);
            int x2 = x + i * graphW / Math.max(1, arr.length - 1);
            int y1g = y + graphH - (int) (arr[i - 1] / 20f * graphH);
            int y2g = y + graphH - (int) (arr[i] / 20f * graphH);
            float ratio = arr[i] / 20f;
            int color = ratio > 0.9f ? 0xFF55FF55 : ratio > 0.5f ? 0xFFFFFF55 : 0xFFFF5555;
            ctx.fill(x1, Math.min(y1g, y2g), x2 + 1, Math.max(y1g, y2g) + 1, color);
        }

        float currentTps = tpsHistory.isEmpty() ? 20f : arr[arr.length - 1];
        ctx.drawTextWithShadow(mc.textRenderer,
                String.format("%.1f", currentTps), x + graphW - 20, y, 0xFFFFFFFF);
    }
}
