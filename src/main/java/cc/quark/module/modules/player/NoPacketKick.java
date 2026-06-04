package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;

public class NoPacketKick extends Module {

    private final BoolSetting autoRespond = register(new BoolSetting(
            "Auto Respond", "Immediately respond to keepalive packets", true));
    private final BoolSetting cancelDisconnect = register(new BoolSetting(
            "Cancel Disconnect", "Cancel incoming disconnect packets", true));
    private final BoolSetting logDisconnect = register(new BoolSetting(
            "Log Disconnect", "Log blocked disconnect reason to chat", true));

    public NoPacketKick() {
        super("NoPacketKick", "Prevents kick packets from disconnecting you; auto-responds to keepalive", Category.PLAYER);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;

        if (autoRespond.isEnabled() && event.getPacket() instanceof KeepAliveS2CPacket pkt) {
            mc.player.networkHandler.sendPacket(new KeepAliveC2SPacket(pkt.id()));
            event.cancel();
        }

        if (cancelDisconnect.isEnabled() && event.getPacket() instanceof DisconnectS2CPacket pkt) {
            if (logDisconnect.isEnabled()) {
                ChatUtil.warn("[NoPacketKick] Blocked disconnect: " + pkt.reason().getString());
            }
            event.cancel();
        }
    }
}
