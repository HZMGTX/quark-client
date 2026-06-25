package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * PingColor - Renders a small overlay showing players from the tab list with
 * their ping color-coded by latency tier:
 *
 *   Green  (&lt;50 ms)
 *   Yellow (50-100 ms)
 *   Orange (100-200 ms)
 *   Red    (&gt;200 ms)
 *
 * The overlay is useful even without opening the tab list.
 */
public class PingColor extends Module {

    private final IntSetting  x          = register(new IntSetting ("X",         "HUD X position",                    2,  0, 1000));
    private final IntSetting  y          = register(new IntSetting ("Y",         "HUD Y position",                    60, 0, 600));
    private final IntSetting  maxPlayers = register(new IntSetting ("Max",       "Maximum players to display",        10, 1, 50));
    private final BoolSetting showSelf   = register(new BoolSetting("Show Self", "Include own entry in the list",     false));
    private final BoolSetting sortByPing = register(new BoolSetting("Sort",      "Sort by ping ascending",            true));

    private final List<PlayerPing> players = new ArrayList<>();

    public PingColor() {
        super("PingColor", "Colors player names by ping in a HUD tab-list overlay", Category.RENDER);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.getNetworkHandler() == null || mc.player == null) return;
        players.clear();
        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            if (!showSelf.isEnabled() && entry.getProfile().getId().equals(mc.player.getUuid())) continue;
            String name = entry.getProfile().getName();
            int ping = entry.getLatency();
            players.add(new PlayerPing(name, ping));
        }
        if (sortByPing.isEnabled()) {
            players.sort(Comparator.comparingInt(p -> p.ping));
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.player == null) return;
        DrawContext ctx = e.getDrawContext();
        int px = x.get();
        int py = y.get();
        int limit = Math.min(players.size(), maxPlayers.get());
        int lineH = 10;

        for (int i = 0; i < limit; i++) {
            PlayerPing pp = players.get(i);
            int col = pingColor(pp.ping);
            String text = pp.name + " " + pp.ping + "ms";
            ctx.drawTextWithShadow(mc.textRenderer, text, px, py + i * lineH, col);
        }
    }

    private static int pingColor(int ping) {
        if (ping < 50)  return 0xFF55FF55;
        if (ping < 100) return 0xFFFFFF55;
        if (ping < 200) return 0xFFFFAA00;
        return 0xFFFF5555;
    }

    private record PlayerPing(String name, int ping) {}
}
