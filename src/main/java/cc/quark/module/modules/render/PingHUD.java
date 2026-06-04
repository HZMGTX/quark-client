package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ServerInfo;

public class PingHUD extends Module {

    private final IntSetting x     = register(new IntSetting("X", "HUD X position", 4, 0, 3840));
    private final IntSetting y     = register(new IntSetting("Y", "HUD Y position", 4, 0, 2160));
    private final BoolSetting color = register(new BoolSetting("Color", "Color-code by ping quality", true));

    public PingHUD() {
        super("PingHUD", "Displays current server ping on the HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        int ping = -1;
        if (mc.getCurrentServerEntry() != null) {
            var entry = mc.getNetworkHandler() == null ? null :
                    mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) ping = entry.getLatency();
        }

        String text = "Ping: " + (ping < 0 ? "N/A" : ping + "ms");
        int clr = 0xFFFFFFFF;
        if (color.isEnabled() && ping >= 0) {
            if      (ping < 50)  clr = 0xFF55FF55;
            else if (ping < 100) clr = 0xFFFFFF55;
            else if (ping < 200) clr = 0xFFFF9944;
            else                 clr = 0xFFFF5555;
        }
        ctx.drawTextWithShadow(mc.textRenderer, text, x.get(), y.get(), clr);
    }
}
