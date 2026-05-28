package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.Deque;

public class ServerInfo extends Module {

    private final BoolSetting showIP      = register(new BoolSetting("Show IP", "Display server address", true));
    private final BoolSetting showMOTD    = register(new BoolSetting("Show MOTD", "Display server MOTD", false));
    private final BoolSetting showPlayers = register(new BoolSetting("Show Players", "Display online player count", true));
    private final BoolSetting showPing    = register(new BoolSetting("Show Ping", "Display your latency", true));
    private final BoolSetting showTPS     = register(new BoolSetting("Show TPS", "Display estimated server TPS", true));
    private final BoolSetting joinMsg     = register(new BoolSetting("Join Message", "Print server info in chat on join", true));
    private final IntSetting  posX        = register(new IntSetting("X", "HUD X offset from right edge", 4, 0, 3000));
    private final IntSetting  posY        = register(new IntSetting("Y", "HUD Y position", 4, 0, 3000));

    private final Deque<Long> tickTimes = new ArrayDeque<>();
    private double estimatedTps = 20.0;
    private boolean hasJoined = false;

    public ServerInfo() {
        super("ServerInfo", "Displays server IP, MOTD, player count, ping, and estimated TPS on the HUD", Category.MISC);
    }

    @Override
    public void onEnable() {
        hasJoined = false;
        estimatedTps = 20.0;
        tickTimes.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) { hasJoined = false; return; }

        long now = System.currentTimeMillis();
        tickTimes.addLast(now);
        while (tickTimes.size() > 20) tickTimes.pollFirst();
        if (tickTimes.size() >= 2) {
            Long[] ts = tickTimes.toArray(new Long[0]);
            long span = ts[ts.length - 1] - ts[0];
            if (span > 0) {
                double avg = (double) span / (ts.length - 1);
                estimatedTps = Math.min(20.0, 1000.0 / avg);
            }
        }

        if (!hasJoined) {
            hasJoined = true;
            if (joinMsg.isEnabled()) sendJoinMessage();
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.getWindow() == null) return;
        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int lh = mc.textRenderer.fontHeight + 2;
        int pad = 4;

        net.minecraft.client.network.ServerInfo si = mc.getCurrentServerEntry();
        String ip = si != null ? si.address : "Singleplayer";
        String motd = si != null && si.label != null ? si.label.getString() : "";

        int playerCount = 0, ping = 0;
        if (mc.getNetworkHandler() != null) {
            playerCount = mc.getNetworkHandler().getPlayerList().size();
            if (mc.player != null) {
                var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                if (entry != null) ping = entry.getLatency();
            }
        }

        java.util.List<String> lines = new java.util.ArrayList<>();
        if (showIP.isEnabled())      lines.add("§7Server: §f" + ip);
        if (showMOTD.isEnabled() && !motd.isEmpty()) lines.add("§7MOTD: §f" + motd);
        if (showPlayers.isEnabled()) lines.add("§7Players: §f" + playerCount);
        if (showPing.isEnabled())    lines.add("§7Ping: " + pingColor(ping) + ping + "ms");
        if (showTPS.isEnabled())     lines.add(String.format("§7TPS: " + tpsColor(estimatedTps) + "%.1f", estimatedTps));

        if (lines.isEmpty()) return;

        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, mc.textRenderer.getWidth(stripFormat(l)));
        int boxW = maxW + pad * 2 + 2;
        int boxH = lines.size() * lh + pad;
        int bx = sw - boxW - posX.get();
        int by = posY.get();

        ctx.fill(bx, by, bx + boxW, by + boxH, 0xBB111111);
        ctx.fill(bx, by, bx + 2, by + boxH, cc.quark.gui.ClickGUI.getAccentColor());

        int ty = by + pad / 2;
        for (int i = 0; i < lines.size(); i++) {
            int col = 0xFFFFFFFF;
            if (showPing.isEnabled() && lines.get(i).contains("Ping:")) {
                col = ping < 50 ? 0xFF55FF55 : ping < 100 ? 0xFFFFFF55 : 0xFFFF5555;
            } else if (showTPS.isEnabled() && lines.get(i).contains("TPS:")) {
                col = estimatedTps >= 19.5 ? 0xFF55FF55 : estimatedTps >= 15.0 ? 0xFFFFFF55 : 0xFFFF5555;
            }
            ctx.drawTextWithShadow(mc.textRenderer, lines.get(i), bx + pad, ty, col);
            ty += lh;
        }
    }

    private void sendJoinMessage() {
        if (mc.player == null) return;
        net.minecraft.client.network.ServerInfo si = mc.getCurrentServerEntry();
        String ip = si != null ? si.address : "Singleplayer";
        int players = mc.getNetworkHandler() != null ? mc.getNetworkHandler().getPlayerList().size() : 0;
        ChatUtil.info("Connected to §f" + ip + " §7| Players: §f" + players);
    }

    private String stripFormat(String s) {
        return s.replaceAll("§[0-9a-fk-or]", "");
    }

    private String pingColor(int ping) {
        if (ping < 50) return "§a";
        if (ping < 100) return "§e";
        if (ping < 200) return "§6";
        return "§c";
    }

    private String tpsColor(double tps) {
        if (tps >= 19.5) return "§a";
        if (tps >= 15.0) return "§e";
        return "§c";
    }
}
