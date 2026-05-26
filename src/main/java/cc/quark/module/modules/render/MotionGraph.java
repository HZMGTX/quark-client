package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;

public class MotionGraph extends Module {

    private final IntSetting posX    = register(new IntSetting("X",      "X position",     0,    0, 4000));
    private final IntSetting posY    = register(new IntSetting("Y",      "Y position",     0,    0, 4000));
    private final IntSetting width   = register(new IntSetting("Width",  "Graph width",  150,   80,  250));
    private final IntSetting height  = register(new IntSetting("Height", "Graph height",  50,   30,  100));
    private final BoolSetting fill   = register(new BoolSetting("Fill",  "Fill under line", true));

    private static final int MAX_SAMPLES = 60;
    private final Deque<Float> samples = new ArrayDeque<>();

    private Vec3d lastPos = null;
    private boolean defaultsInitialized = false;

    public MotionGraph() {
        super("MotionGraph", "Displays a real-time BPS graph on the HUD", Category.RENDER);
    }

    @Override
    public void onEnable() {
        lastPos = null;
        samples.clear();
        defaultsInitialized = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (!defaultsInitialized && mc.getWindow() != null) {
            int sw = mc.getWindow().getScaledWidth();
            int sh = mc.getWindow().getScaledHeight();
            posX.setValue(sw - 160);
            posY.setValue(sh - 60);
            defaultsInitialized = true;
        }

        Vec3d pos = mc.player.getPos();
        float bps = 0f;
        if (lastPos != null) {
            double dx = pos.x - lastPos.x;
            double dz = pos.z - lastPos.z;
            bps = (float) (Math.sqrt(dx * dx + dz * dz) * 20.0);
        }
        lastPos = pos;

        samples.addLast(bps);
        while (samples.size() > MAX_SAMPLES) {
            samples.pollFirst();
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.getWindow() == null) return;
        DrawContext ctx = event.getDrawContext();

        int gx = posX.get();
        int gy = posY.get();
        int gw = width.get();
        int gh = height.get();

        int bg = 0xAA101010;
        ctx.fill(gx, gy, gx + gw, gy + gh, bg);

        if (samples.isEmpty()) return;

        Float[] arr = samples.toArray(new Float[0]);
        int count = arr.length;

        float maxVal = 0.001f;
        for (float v : arr) {
            if (v > maxVal) maxVal = v;
        }

        float currentBps = arr[count - 1];
        int accentColor = 0xFF00CFFF;
        int fillColor   = 0x5500CFFF;

        int innerX = gx + 1;
        int innerY = gy + 1;
        int innerW = gw - 2;
        int innerH = gh - 2 - mc.textRenderer.fontHeight - 2;

        for (int i = 0; i < count - 1; i++) {
            float t0 = (float) i / (MAX_SAMPLES - 1);
            float t1 = (float) (i + 1) / (MAX_SAMPLES - 1);

            int x0 = innerX + (int) (t0 * innerW);
            int x1 = innerX + (int) (t1 * innerW);

            float h0 = arr[i] / maxVal;
            float h1 = arr[i + 1] / maxVal;
            h0 = Math.min(1f, Math.max(0f, h0));
            h1 = Math.min(1f, Math.max(0f, h1));

            int y0 = innerY + innerH - (int) (h0 * innerH);
            int y1 = innerY + innerH - (int) (h1 * innerH);

            if (fill.isEnabled()) {
                int fillBtm = innerY + innerH;
                int lx0 = Math.min(x0, x1);
                int lx1 = Math.max(x0, x1) + 1;
                int ly0 = Math.min(y0, y1);
                ctx.fill(lx0, ly0, lx1, fillBtm, fillColor);
            }

            drawLine2D(ctx, x0, y0, x1, y1, accentColor);
        }

        int labelY = gy + gh - mc.textRenderer.fontHeight - 1;
        ctx.drawTextWithShadow(mc.textRenderer, "BPS", gx + 2, labelY, 0xFFAAAAAA);

        String bpsText = String.format("%.1f", currentBps);
        int bpsW = mc.textRenderer.getWidth(bpsText);
        ctx.drawTextWithShadow(mc.textRenderer, bpsText, gx + gw - bpsW - 2, gy + 1, accentColor);
    }

    private void drawLine2D(DrawContext ctx, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        if (dx == 0 && dy == 0) {
            ctx.fill(x0, y0, x0 + 1, y0 + 1, color);
            return;
        }
        if (dx >= dy) {
            if (x0 > x1) { int t = x0; x0 = x1; x1 = t; t = y0; y0 = y1; y1 = t; }
            for (int x = x0; x <= x1; x++) {
                int y = y0 + (x - x0) * (y1 - y0) / (dx == 0 ? 1 : dx);
                ctx.fill(x, y, x + 1, y + 1, color);
            }
        } else {
            if (y0 > y1) { int t = y0; y0 = y1; y1 = t; t = x0; x0 = x1; x1 = t; }
            for (int y = y0; y <= y1; y++) {
                int x = x0 + (y - y0) * (x1 - x0) / (dy == 0 ? 1 : dy);
                ctx.fill(x, y, x + 1, y + 1, color);
            }
        }
    }
}
