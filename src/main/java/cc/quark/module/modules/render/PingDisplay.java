package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public class PingDisplay extends Module {

    private final IntSetting x = register(new IntSetting("X", "X pos", 4, 0, 500));
    private final IntSetting y = register(new IntSetting("Y", "Y pos", 14, 0, 500));

    public PingDisplay() {
        super("PingDisplay", "Shows the local player's network latency", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        DrawContext ctx = event.getDrawContext();
        int ping = 0;
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (entry != null) ping = entry.getLatency();
        ctx.drawTextWithShadow(mc.textRenderer, "Ping: " + ping + "ms", x.get(), y.get(), 0xFFFFFFFF);
    }
}
