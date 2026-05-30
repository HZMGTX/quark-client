package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public class ConnectionInfo extends Module {

    private final IntSetting posX = register(new IntSetting(
            "X", "HUD X position", 4, 0, 500));
    private final IntSetting posY = register(new IntSetting(
            "Y", "HUD Y position", 44, 0, 500));

    public ConnectionInfo() {
        super("ConnectionInfo", "Displays server ping/latency as a HUD overlay", Category.MISC);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        int ping = entry != null ? entry.getLatency() : -1;

        String text;
        int color;
        if (ping < 0) {
            text = "Ping: N/A";
            color = 0xFFAAAAAA;
        } else {
            text = "Ping: " + ping + "ms";
            color = ping < 50 ? 0xFF55FF55 : ping < 100 ? 0xFFFFFF55 : 0xFFFF5555;
        }

        DrawContext ctx = event.getDrawContext();
        ctx.drawTextWithShadow(mc.textRenderer, text, posX.get(), posY.get(), color);
    }
}
