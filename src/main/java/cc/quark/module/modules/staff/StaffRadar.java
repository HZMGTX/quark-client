package cc.quark.module.modules.staff;

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
import java.util.List;

public class StaffRadar extends Module {
    private final IntSetting x = register(new IntSetting("X", "HUD X", 5, 0, 1920));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y", 30, 0, 1080));
    private final IntSetting maxPlayers = register(new IntSetting("Max Players", "Max players to show", 20, 5, 100));
    private final BoolSetting showPing = register(new BoolSetting("Show Ping", "Show ping next to name", true));
    private final BoolSetting showDistance = register(new BoolSetting("Show Distance", "Show block distance", true));

    private final List<String> playerEntries = new ArrayList<>();

    public StaffRadar() {
        super("Staff Radar", "HUD list of all online players with ping/distance", Category.STAFF, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        playerEntries.clear();
        int count = 0;
        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            if (count++ >= maxPlayers.get()) break;
            String name = entry.getProfile().getName();
            if (name.equals(mc.player.getName().getString())) continue;
            String info = name;
            if (showPing.isEnabled()) info += " §7[" + entry.getLatency() + "ms]";
            if (showDistance.isEnabled() && mc.world != null) {
                for (var p : mc.world.getPlayers()) {
                    if (p.getName().getString().equals(name)) {
                        int dist = (int) mc.player.distanceTo(p);
                        info += " §8" + dist + "m";
                        break;
                    }
                }
            }
            playerEntries.add(info);
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        ctx.drawText(mc.textRenderer, "§6§l[Staff Radar]", x.get(), y.get(), 0xFFFFFF, true);
        int yOff = 12;
        for (String entry : playerEntries) {
            ctx.drawText(mc.textRenderer, "§7• §f" + entry, x.get(), y.get() + yOff, 0xFFFFFF, true);
            yOff += 10;
        }
    }
}
