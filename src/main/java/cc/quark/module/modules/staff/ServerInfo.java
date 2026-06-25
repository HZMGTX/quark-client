package cc.quark.module.modules.staff;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ServerInfo extends Module {

    private final BoolSetting showTime    = register(new BoolSetting("ShowTime", "ShowTime", true));
    private final BoolSetting showPlayers = register(new BoolSetting("ShowPlayers", "ShowPlayers", true));
    private final BoolSetting showTPS     = register(new BoolSetting("ShowTPS", "ShowTPS", true));
    private final BoolSetting showPing    = register(new BoolSetting("ShowPing", "ShowPing", true));
    private final IntSetting posX = register(new IntSetting("X", "X", 4, 0, 800));
    private final IntSetting posY = register(new IntSetting("Y", "Y", 4, 0, 600));

    private double tps = 20.0;
    private long lastTimeUpdate = 0;
    private long lastWorldTime = 0;

    public ServerInfo() {
        super("ServerInfo", "Displays server info overlay: TPS, players, time, ping", Category.STAFF);
    }


    @EventHandler
    public void onTick(EventTick event) {
        
        if (mc == null || mc.world == null) return;
        long worldTime = mc.world.getTime();
        if (worldTime != lastWorldTime) {
            long now = System.currentTimeMillis();
            if (lastTimeUpdate > 0) {
                long delta = now - lastTimeUpdate;
                tps = Math.min(20.0, 20.0 * 1000.0 / delta);
            }
            lastWorldTime = worldTime;
            lastTimeUpdate = now;
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        
        if (mc == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int x = posX.get(), y = posY.get();
        int lineH = 10;

        ctx.drawTextWithShadow(mc.textRenderer, "§b[ServerInfo]", x, y, 0xFFFFFFFF); y += lineH;

        if (showTPS.isEnabled()) {
            String tpsColor = tps >= 18 ? "§a" : tps >= 12 ? "§e" : "§c";
            ctx.drawTextWithShadow(mc.textRenderer, "TPS: " + tpsColor + String.format("%.1f", tps), x, y, 0xFFFFFFFF); y += lineH;
        }
        if (showPlayers.isEnabled() && mc.world != null) {
            ctx.drawTextWithShadow(mc.textRenderer, "Players: §f" + mc.world.getPlayers().size(), x, y, 0xFFAAAAAA); y += lineH;
        }
        if (showPing.isEnabled() && mc.getNetworkHandler() != null) {
            var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            int ping = entry != null ? entry.getLatency() : 0;
            String pingColor = ping < 80 ? "§a" : ping < 150 ? "§e" : "§c";
            ctx.drawTextWithShadow(mc.textRenderer, "Ping: " + pingColor + ping + "ms", x, y, 0xFFFFFFFF); y += lineH;
        }
        if (showTime.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, "Time: §f" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), x, y, 0xFFFFFFFF);
        }
    }
}
