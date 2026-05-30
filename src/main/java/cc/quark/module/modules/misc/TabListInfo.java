package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ServerInfo;

public class TabListInfo extends Module {

    private final IntSetting posX = register(new IntSetting(
            "X", "HUD X position", 4, 0, 500));
    private final IntSetting posY = register(new IntSetting(
            "Y", "HUD Y position", 54, 0, 500));

    public TabListInfo() {
        super("TabListInfo", "Displays player count and server address as a HUD overlay", Category.MISC);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.getNetworkHandler() == null) return;

        DrawContext ctx = event.getDrawContext();
        int x = posX.get();
        int y = posY.get();
        int lh = mc.textRenderer.fontHeight + 2;

        int players = mc.getNetworkHandler().getPlayerList().size();
        ctx.drawTextWithShadow(mc.textRenderer, "Players: " + players, x, y, 0xFFFFFFFF);

        ServerInfo serverEntry = mc.getCurrentServerEntry();
        if (serverEntry != null) {
            ctx.drawTextWithShadow(mc.textRenderer, "Server: " + serverEntry.address, x, y + lh, 0xFFAAAAAA);
        }
    }
}
