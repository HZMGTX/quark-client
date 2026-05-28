package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.c2s.play.PlayPongC2SPacket;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.LoginCompressionS2CPacket;

public class NoPacketKick extends Module {

    private final BoolSetting autoRespond = register(new BoolSetting("Auto Respond", "Auto-respond to keepalive packets", true));

    public NoPacketKick() {
        super("NoPacketKick", "Prevents kick from keepalive timeout by auto-responding", Category.PLAYER);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;

        if (autoRespond.isEnabled() && event.getPacket() instanceof KeepAliveS2CPacket pkt) {
            mc.player.networkHandler.sendPacket(new PlayPongC2SPacket((int) pkt.getId()));
        }

        if (event.getPacket() instanceof LoginCompressionS2CPacket) {
            event.cancel();
        }
    }
}
