package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.gui.DrawContext;

public class ServerInfo extends Module {

    private final BoolSetting joinMessage = register(new BoolSetting("Join Message", "Announce server info in chat on join", true));
    private final BoolSetting hudDisplay  = register(new BoolSetting("HUD Display",  "Show server info on screen",           true));
    private final BoolSetting tpsDisplay  = register(new BoolSetting("TPS Display",  "Show estimated TPS",                   true));

    private boolean hasJoined = false;

    private double estimatedTps = 20.0;
    private int tpsSampleCount = 0;
    private final long[] tickTimes = new long[20];
    private int tickIndex = 0;

    public ServerInfo() {
        super("ServerInfo", "Displays server information on join and in the HUD", Category.MISC);
    }

    @Override
    public void onEnable() {
        hasJoined = false;
        estimatedTps = 20.0;
        tpsSampleCount = 0;
        tickIndex = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) {
            hasJoined = false;
            return;
        }

        long now = System.currentTimeMillis();
        if (tpsSampleCount < 20) {
            tickTimes[tickIndex % 20] = now;
            tpsSampleCount++;
        } else {
            tickTimes[tickIndex % 20] = now;
            long oldest = tickTimes[(tickIndex + 1) % 20];
            long elapsed = now - oldest;
            if (elapsed > 0) {
                estimatedTps = Math.min(20.0, 20000.0 / elapsed);
            }
        }
        tickIndex++;

        if (!hasJoined) {
            hasJoined = true;
            if (joinMessage.isEnabled()) {
                sendJoinMessage();
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!hudDisplay.isEnabled() || mc.getWindow() == null) return;
        DrawContext ctx = event.getDrawContext();

        net.minecraft.client.network.ServerInfo si = mc.getCurrentServerEntry();
        String ip   = si != null ? si.address : "Singleplayer";
        String motd = si != null && si.label != null ? si.label.getString() : "";

        int playerCount = 0;
        int ping = 0;
        if (mc.getNetworkHandler() != null) {
            playerCount = mc.getNetworkHandler().getPlayerList().size();
            if (mc.player != null) {
                var selfEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                if (selfEntry != null) ping = selfEntry.getLatency();
            }
        }

        int sw    = mc.getWindow().getScaledWidth();
        int lineH = mc.textRenderer.fontHeight + 2;
        int pad   = 4;

        String ipLine      = "§7Server: §f" + ip;
        String playersLine = "§7Players: §f" + playerCount;
        String pingLine    = "§7Ping: " + pingColor(ping) + ping + "ms";
        String tpsLine     = tpsDisplay.isEnabled()
                ? String.format("§7TPS: §r%.1f", estimatedTps) : null;

        int maxW = mc.textRenderer.getWidth(stripFormat(ipLine));
        maxW = Math.max(maxW, mc.textRenderer.getWidth(stripFormat(playersLine)));
        maxW = Math.max(maxW, mc.textRenderer.getWidth(stripFormat(pingLine)));
        if (tpsLine != null) maxW = Math.max(maxW, mc.textRenderer.getWidth(stripFormat(tpsLine)));
        if (!motd.isEmpty()) maxW = Math.max(maxW, mc.textRenderer.getWidth(motd));

        int lineCount = 3 + (motd.isEmpty() ? 0 : 1) + (tpsLine != null ? 1 : 0);
        int boxW = maxW + pad * 2 + 2;
        int boxH = lineCount * lineH + pad;
        int bx   = sw - boxW - 4;
        int by   = 4;

        ctx.fill(bx, by, bx + boxW, by + boxH, 0xBB111111);
        ctx.fill(bx, by, bx + 2, by + boxH, cc.quark.gui.ClickGUI.getAccentColor());

        int ty = by + pad / 2;
        ctx.drawTextWithShadow(mc.textRenderer, ipLine,      bx + pad, ty, 0xFFFFFFFF); ty += lineH;
        if (!motd.isEmpty()) {
            ctx.drawTextWithShadow(mc.textRenderer, "§7MOTD: §f" + motd, bx + pad, ty, 0xFFFFFFFF); ty += lineH;
        }
        ctx.drawTextWithShadow(mc.textRenderer, playersLine, bx + pad, ty, 0xFFFFFFFF); ty += lineH;
        ctx.drawTextWithShadow(mc.textRenderer, pingLine,    bx + pad, ty, 0xFFFFFFFF); ty += lineH;
        if (tpsLine != null) {
            int tpsColor = estimatedTps >= 19.5 ? 0xFF55FF55 : estimatedTps >= 15.0 ? 0xFFFFFF55 : 0xFFFF5555;
            ctx.drawTextWithShadow(mc.textRenderer, tpsLine, bx + pad, ty, tpsColor);
        }
    }

    private void sendJoinMessage() {
        if (mc.player == null) return;
        net.minecraft.client.network.ServerInfo si = mc.getCurrentServerEntry();
        String ip = si != null ? si.address : "Singleplayer";
        int players = mc.getNetworkHandler() != null ? mc.getNetworkHandler().getPlayerList().size() : 0;
        ChatUtil.info("§7[Server] §f" + ip + " §7| Players: §f" + players);
    }

    private String stripFormat(String s) {
        return s.replaceAll("§[0-9a-fk-or]", "");
    }

    private String pingColor(int ping) {
        if (ping < 50)  return "§a";
        if (ping < 100) return "§e";
        if (ping < 200) return "§6";
        return "§c";
    }
}
