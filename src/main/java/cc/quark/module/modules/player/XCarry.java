package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class XCarry extends Module {

    public XCarry() {
        super("XCarry", "Prevents inventory close packet to carry extra items", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;
        if (mc.currentScreen == null) return;
        if (event.getPacket() instanceof CloseHandledScreenC2SPacket) {
            event.cancel();
        }
    }
}
