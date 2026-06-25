package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.Deque;

public class StaffDashboard extends Module {

    private final IntSetting x = register(new IntSetting(
            "X", "Horizontal position of the dashboard HUD", 4, 0, 1920));
    private final IntSetting y = register(new IntSetting(
            "Y", "Vertical position of the dashboard HUD", 4, 0, 1080));
    private final BoolSetting compact = register(new BoolSetting(
            "Compact", "Show a smaller single-line summary instead of the full panel", false));
    private final BoolSetting showFlags = register(new BoolSetting(
            "Show Flags", "Display the recent violation/flag log on the HUD", true));

    // Rolling TPS sample (last 20 values, computed from tick timing)
    private final Deque<Double> tpsSamples = new ArrayDeque<>(20);
    private long lastTickTime = -1;
    private double currentTps = 20.0;

    // Recent flag log (capacity 5)
    private final Deque<String> flags = new ArrayDeque<>(5);
    private int onlineCount = 0;
    private int tickCount = 0;

    public StaffDashboard() {
        super("StaffDashboard", "HUD overlay showing player count, TPS, recent flags, and active ban info", Category.STAFF);
    }

    @Override
    public void onEnable() {
        tpsSamples.clear();
        flags.clear();
        lastTickTime = -1;
        tickCount = 0;
    }

    @Override
    public void onDisable() {
        tpsSamples.clear();
    }

    /** Add a flag entry (called from other modules or future hooks). */
    public void addFlag(String description) {
        if (flags.size() >= 5) flags.pollFirst();
        flags.addLast("§e" + description);
    }

    @EventHandler
    public void onTick(EventTick event) {
        tickCount++;

        // Measure TPS via tick delta timing
        long now = System.currentTimeMillis();
        if (lastTickTime > 0) {
            long delta = now - lastTickTime;
            double tps = Math.min(20.0, 1000.0 / delta);
            if (tpsSamples.size() >= 20) tpsSamples.pollFirst();
            tpsSamples.addLast(tps);
            currentTps = tpsSamples.stream().mapToDouble(Double::doubleValue).average().orElse(20.0);
        }
        lastTickTime = now;

        // Update online player count once per second
        if (tickCount % 20 == 0 && mc.getNetworkHandler() != null) {
            onlineCount = mc.getNetworkHandler().getPlayerList().size();
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int px = x.get();
        int py = y.get();

        String tpsColor = currentTps >= 19.0 ? "§a" : currentTps >= 15.0 ? "§e" : "§c";
        String tpsStr = tpsColor + String.format("%.1f", currentTps) + " TPS";

        if (compact.isEnabled()) {
            String line = "§b[STAFF] §fPlayers: §e" + onlineCount + "  " + tpsStr;
            RenderUtil.drawCustomText(ctx, line, px, py, 0xFFFFFFFF);
            return;
        }

        // Background panel
        int panelW = 160;
        int panelH = showFlags.isEnabled() ? (flags.isEmpty() ? 52 : 52 + flags.size() * 10) : 52;
        ctx.fill(px - 2, py - 2, px + panelW, py + panelH, 0xAA000000);
        ctx.fill(px - 2, py - 2, px + panelW, py - 1, 0xFF0088CC); // top accent bar

        // Title
        RenderUtil.drawCustomText(ctx, "§b§lStaff Dashboard", px, py, 0xFFFFFFFF);
        py += 11;

        // Player count
        RenderUtil.drawCustomText(ctx, "§7Players Online: §f" + onlineCount, px, py, 0xFFFFFFFF);
        py += 10;

        // TPS
        RenderUtil.drawCustomText(ctx, "§7Server TPS: " + tpsStr, px, py, 0xFFFFFFFF);
        py += 10;

        // Ping to server
        int ping = mc.player.networkHandler != null && mc.player.getPlayerListEntry() != null
                ? mc.player.getPlayerListEntry().getLatency() : -1;
        String pingStr = ping >= 0 ? ping + " ms" : "N/A";
        RenderUtil.drawCustomText(ctx, "§7Ping: §f" + pingStr, px, py, 0xFFFFFFFF);
        py += 10;

        // Recent flags
        if (showFlags.isEnabled() && !flags.isEmpty()) {
            py += 2;
            RenderUtil.drawCustomText(ctx, "§c§lFlags:", px, py, 0xFFFFFFFF);
            py += 10;
            for (String flag : flags) {
                RenderUtil.drawCustomText(ctx, "§7> " + flag, px, py, 0xFFFFFFFF);
                py += 10;
            }
        }
    }
}
