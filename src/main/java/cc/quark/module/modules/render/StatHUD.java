package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public class StatHUD extends Module {

    private final ModeSetting position = register(new ModeSetting("Position", "HUD corner position",
            "TopLeft", "TopLeft", "TopRight", "BottomLeft", "BottomRight"));

    private double prevX, prevZ;
    private double bps;
    private boolean firstTick = true;
    private long lastTickTime = System.currentTimeMillis();
    private long lastServerTick = System.currentTimeMillis();
    private double tps = 20.0;

    public StatHUD() {
        super("StatHUD", "Compact HUD showing ping, fps, tps, and coords in one corner", Category.RENDER);
    }

    @Override
    public void onEnable() {
        firstTick = true;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (firstTick) {
            prevX = mc.player.getX(); prevZ = mc.player.getZ(); firstTick = false; return;
        }
        double dx = mc.player.getX() - prevX;
        double dz = mc.player.getZ() - prevZ;
        bps = Math.sqrt(dx * dx + dz * dz) * 20.0;
        prevX = mc.player.getX(); prevZ = mc.player.getZ();

        long now = System.currentTimeMillis();
        long delta = now - lastTickTime;
        if (delta > 0) tps = Math.min(20.0, 1000.0 / delta * 1.0);
        lastTickTime = now;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        int ping = 0;
        if (mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) ping = entry.getLatency();
        }

        int fps = mc.getCurrentFps();
        String[] lines = {
            String.format("XYZ: %.0f / %.0f / %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ()),
            String.format("FPS: %d  Ping: %dms", fps, ping),
            String.format("TPS: %.1f  Spd: %.1f", tps, bps)
        };

        int lh = mc.textRenderer.fontHeight + 2;
        int totalH = lines.length * lh;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, mc.textRenderer.getWidth(l));

        String pos = position.get();
        int x, y;
        if (pos.equals("TopLeft"))     { x = 4; y = 4; }
        else if (pos.equals("TopRight"))  { x = sw - maxW - 4; y = 4; }
        else if (pos.equals("BottomLeft")) { x = 4; y = sh - totalH - 4; }
        else                              { x = sw - maxW - 4; y = sh - totalH - 4; }

        ctx.fill(x - 2, y - 2, x + maxW + 2, y + totalH, 0x88000000);
        for (String line : lines) {
            ctx.drawTextWithShadow(mc.textRenderer, line, x, y, 0xFFFFFFFF);
            y += lh;
        }
    }
}
