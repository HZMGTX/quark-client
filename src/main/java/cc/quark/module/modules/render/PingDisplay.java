package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public class PingDisplay extends Module {

    private final IntSetting  posX      = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting  posY      = register(new IntSetting("Y", "HUD Y position", 14, 0, 3000));
    private final BoolSetting showBar   = register(new BoolSetting("Bar", "Show latency quality bar", false));

    public PingDisplay() {
        super("PingDisplay", "Shows server ping with color coding: green <50ms, yellow <100ms, red >100ms", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        DrawContext ctx = event.getDrawContext();

        int ping = 0;
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (entry != null) ping = entry.getLatency();

        int color = ping < 50 ? 0xFF55FF55 : ping < 100 ? 0xFFFFFF55 : ping < 200 ? 0xFFFF8844 : 0xFFFF5555;
        int x = posX.get(), y = posY.get();

        ctx.drawTextWithShadow(mc.textRenderer, "Ping: " + ping + "ms", x, y, color);

        if (showBar.isEnabled()) {
            int barW = 50;
            int quality = ping < 50 ? 5 : ping < 100 ? 4 : ping < 150 ? 3 : ping < 200 ? 2 : 1;
            for (int i = 0; i < 5; i++) {
                int bh = 3 + i * 2;
                int bx = x + i * 6;
                int by = y + mc.textRenderer.fontHeight + 1;
                int col = i < quality ? color : 0xFF333333;
                ctx.fill(bx, by + (10 - bh), bx + 4, by + 10, col);
            }
        }
    }
}
