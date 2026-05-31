package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * VelocityHUD — displays the player's current velocity vector as an overlay,
 * including horizontal speed (BPS), vertical speed, and a graphical history
 * bar showing speed over the last N ticks.
 */
public class VelocityHUD extends Module {

    private final IntSetting xPos = register(new IntSetting(
            "X", "Horizontal position", 5, 0, 3000));

    private final IntSetting yPos = register(new IntSetting(
            "Y", "Vertical position (from bottom)", 70, 0, 3000));

    private final BoolSetting showHorizontal = register(new BoolSetting(
            "Horizontal", "Show horizontal (XZ) speed in BPS", true));

    private final BoolSetting showVertical = register(new BoolSetting(
            "Vertical", "Show vertical (Y) velocity", true));

    private final BoolSetting showVector = register(new BoolSetting(
            "Vector", "Show raw XYZ velocity components", false));

    private final BoolSetting showGraph = register(new BoolSetting(
            "Graph", "Show a small speed history bar chart", true));

    private final IntSetting graphWidth = register(new IntSetting(
            "Graph Width", "Width of the speed graph in pixels", 80, 20, 200));

    private final IntSetting graphHeight = register(new IntSetting(
            "Graph Height", "Height of the speed graph in pixels", 24, 8, 60));

    private final ModeSetting colorMode = register(new ModeSetting(
            "Color", "Text/graph color scheme", "Speed", "Speed", "Static", "Rainbow"));

    private final BoolSetting showBackground = register(new BoolSetting(
            "Background", "Draw dark background panel", true));

    private final Deque<Double> speedHistory = new ArrayDeque<>();
    private double prevX, prevZ;
    private double currentBps;
    private double currentVy;

    public VelocityHUD() {
        super("VelocityHUD", "Shows current velocity vector overlay", Category.RENDER);
    }

    @Override
    public void onEnable() {
        speedHistory.clear();
        if (mc.player != null) {
            prevX = mc.player.getX();
            prevZ = mc.player.getZ();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        ClientPlayerEntity player = mc.player;

        double dx = player.getX() - prevX;
        double dz = player.getZ() - prevZ;
        currentBps = Math.sqrt(dx * dx + dz * dz) * 20.0;
        currentVy  = player.getVelocity().y;
        prevX = player.getX();
        prevZ = player.getZ();

        speedHistory.addLast(currentBps);
        if (speedHistory.size() > graphWidth.get()) speedHistory.pollFirst();
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        ClientPlayerEntity player = mc.player;

        DrawContext ctx = event.getDrawContext();
        int screenH = mc.getWindow().getScaledHeight();
        int x = xPos.get();
        int y = screenH - yPos.get();
        int lineH = mc.textRenderer.fontHeight + 2;

        java.util.List<String> lines = new java.util.ArrayList<>();
        if (showHorizontal.isEnabled()) {
            int color = getSpeedColorCode(currentBps);
            lines.add(String.format("§%xBPS: §f%.3f", color, currentBps));
        }
        if (showVertical.isEnabled()) {
            String vy = String.format("%.3f", currentVy);
            String prefix = currentVy > 0.01 ? "§a" : currentVy < -0.01 ? "§c" : "§7";
            lines.add(prefix + "VY: §f" + vy);
        }
        if (showVector.isEnabled()) {
            lines.add(String.format("§7X: §f%.3f", player.getVelocity().x));
            lines.add(String.format("§7Z: §f%.3f", player.getVelocity().z));
        }

        int maxW = lines.stream()
                .mapToInt(l -> mc.textRenderer.getWidth(net.minecraft.text.Text.of(l)))
                .max().orElse(graphWidth.get());
        int panelW = Math.max(maxW + 6, graphWidth.get() + 6);
        int panelH = lines.size() * lineH + (showGraph.isEnabled() ? graphHeight.get() + 4 : 0) + 4;

        if (showBackground.isEnabled()) {
            ctx.fill(x - 3, y - 2, x + panelW, y + panelH, 0xAA111111);
        }

        // Text lines
        int ty = y;
        for (String line : lines) {
            ctx.drawTextWithShadow(mc.textRenderer, net.minecraft.text.Text.of(line), x, ty, 0xFFFFFFFF);
            ty += lineH;
        }

        // Speed graph
        if (showGraph.isEnabled() && !speedHistory.isEmpty()) {
            double maxSpeed = speedHistory.stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
            if (maxSpeed < 1.0) maxSpeed = 1.0;

            int gx = x;
            int gy = ty + 2;
            int gh = graphHeight.get();
            int gw = graphWidth.get();

            ctx.fill(gx, gy, gx + gw, gy + gh, 0x55000000);

            Double[] hist = speedHistory.toArray(new Double[0]);
            int barW = Math.max(1, gw / hist.length);
            for (int i = 0; i < hist.length; i++) {
                double pct = hist[i] / maxSpeed;
                int barH = (int)(gh * pct);
                int bx = gx + i * barW;
                int barColor = getSpeedColorArgb(hist[i]);
                ctx.fill(bx, gy + gh - barH, bx + barW - 1, gy + gh, barColor);
            }
        }
    }

    /** Returns a single hex digit for chat formatting color based on speed. */
    private int getSpeedColorCode(double bps) {
        if (colorMode.get().equals("Static")) return 'f';
        if (colorMode.get().equals("Rainbow")) return 'd';
        // Speed-based
        if (bps > 10.0) return 'c'; // red
        if (bps > 6.0)  return '6'; // orange
        if (bps > 3.0)  return 'e'; // yellow
        if (bps > 0.5)  return 'a'; // green
        return '7'; // grey
    }

    private int getSpeedColorArgb(double bps) {
        if (colorMode.get().equals("Rainbow")) {
            float hue = (System.currentTimeMillis() % 2000L) / 2000.0f;
            return java.awt.Color.HSBtoRGB(hue % 1.0f, 1.0f, 1.0f) | 0xFF000000;
        }
        if (bps > 10.0) return 0xFFFF3333;
        if (bps > 6.0)  return 0xFFFFAA00;
        if (bps > 3.0)  return 0xFFFFFF44;
        if (bps > 0.5)  return 0xFF55FF55;
        return 0xFF888888;
    }
}
