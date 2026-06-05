package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class PingDisplay extends Module {
    private final IntSetting x = register(new IntSetting("X", "HUD X position", 2, 0, 1000));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 2, 0, 600));
    private long ping = 0;

    public PingDisplay() { super("PingDisplay", "Shows your current server ping on HUD", Category.RENDER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (entry != null) ping = entry.getLatency();
    }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.player == null) return;
        DrawContext ctx = e.getDrawContext();
        int pingColor = ping < 50 ? 0xFF55FF55 : (ping < 100 ? 0xFFFFFF55 : (ping < 200 ? 0xFFFFAA00 : 0xFFFF5555));
        cc.quark.util.RenderUtil.drawCustomText(ctx, "Ping: " + ping + "ms", x.get(), y.get(), pingColor);
    }
}
