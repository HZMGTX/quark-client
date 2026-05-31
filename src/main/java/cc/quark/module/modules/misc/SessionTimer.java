package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;

public class SessionTimer extends Module {

    private final BoolSetting showInHUD         = register(new BoolSetting("ShowInHUD",         "Display session timer on the HUD", true));
    private final BoolSetting resetOnDisconnect = register(new BoolSetting("ResetOnDisconnect", "Reset timer when disconnected from server", true));

    private long sessionStart = System.currentTimeMillis();

    public SessionTimer() {
        super("SessionTimer", "Tracks and displays play session duration", Category.MISC);
    }

    @Override
    public void onEnable() {
        sessionStart = System.currentTimeMillis();
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof DisconnectS2CPacket)) return;
        mc.execute(() -> {
            if (resetOnDisconnect.isEnabled()) {
                sessionStart = System.currentTimeMillis();
            }
        });
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showInHUD.isEnabled()) return;
        DrawContext ctx = event.getDrawContext();

        long elapsed = System.currentTimeMillis() - sessionStart;
        long hours   = elapsed / 3_600_000;
        long minutes = (elapsed % 3_600_000) / 60_000;
        long seconds = (elapsed % 60_000) / 1_000;

        String text = String.format("§7Session: §f%02d:%02d:%02d", hours, minutes, seconds);
        int x = 4;
        int y = mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight - 4;
        ctx.drawTextWithShadow(mc.textRenderer, text, x, y, 0xFFFFFFFF);
    }

    @Override
    public String getSuffix() {
        long elapsed = System.currentTimeMillis() - sessionStart;
        long hours   = elapsed / 3_600_000;
        long minutes = (elapsed % 3_600_000) / 60_000;
        long seconds = (elapsed % 60_000) / 1_000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
