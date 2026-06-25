package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

import java.util.Collection;

public class ServerPinger extends Module {
    private final BoolSetting showAvg = register(new BoolSetting("ShowAverage", "Show average ping", true));
    public ServerPinger() { super("ServerPinger", "Displays detailed ping statistics on HUD", Category.MISC); }
    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        Collection<PlayerListEntry> players = mc.getNetworkHandler().getPlayerList();
        int total = players.stream().mapToInt(PlayerListEntry::getLatency).sum();
        int avg = players.isEmpty() ? 0 : total / players.size();
        DrawContext ctx = event.getDrawContext();
        int x = 5, y = ctx.getScaledWindowHeight() - 25;
        ctx.drawText(mc.textRenderer, "Avg Ping: " + avg + "ms", x, y, avg < 100 ? 0xFF44FF44 : avg < 200 ? 0xFFFFFF44 : 0xFFFF4444, true);
    }
}
