package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * Blink2 - holds back outgoing movement packets while enabled, freezing the
 * player's position on the server until the module is toggled off.
 */
public class Blink2 extends Module {

    public Blink2() {
        super("Blink2", "Withholds movement packets", Category.MOVEMENT);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            event.cancel();
        }
    }
}
