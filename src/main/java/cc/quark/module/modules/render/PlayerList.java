package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import java.util.List;

public class PlayerList extends Module {
    private final IntSetting maxPlayers = register(new IntSetting("Max", "Max players to show", 10, 1, 50));
    private final BoolSetting showPing = register(new BoolSetting("Ping", "Show ping", true));

    public PlayerList() { super("PlayerList", "Compact player list HUD overlay", Category.RENDER); }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        DrawContext ctx = e.getDrawContext();
        int sw = ctx.getScaledWindowWidth();
        List<PlayerListEntry> players = mc.getNetworkHandler().getPlayerList().stream()
            .limit(maxPlayers.get()).toList();
        int px = sw - 100, py = 4;
        ctx.fill(px - 2, py - 2, sw - 2, py + players.size() * 10 + 2, ColorUtil.withAlpha(0x111111, 160));
        for (var p : players) {
            String name = p.getProfile().getName();
            String line = showPing.isEnabled() ? name + " " + p.getLatency() + "ms" : name;
            int pingColor = p.getLatency() < 80 ? 0xFF55FF55 : (p.getLatency() < 150 ? 0xFFFFFF55 : 0xFFFF5555);
            cc.quark.util.RenderUtil.drawCustomText(ctx, line, px, py, pingColor);
            py += 10;
        }
    }
}
