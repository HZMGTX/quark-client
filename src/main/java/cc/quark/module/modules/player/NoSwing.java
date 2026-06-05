package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;

public class NoSwing extends Module {
    public NoSwing() { super("NoSwing", "Disables hand swing animation from being sent", Category.PLAYER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onPacketSend(EventPacketSend e) {
        if (e.getPacket() instanceof HandSwingC2SPacket) e.cancel();
    }
}
