package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

public class AntiVelocity extends Module {
    private final DoubleSetting hMult = register(new DoubleSetting("H Mult", "Horizontal velocity multiplier", 0.0, 0.0, 1.0));
    private final DoubleSetting vMult = register(new DoubleSetting("V Mult", "Vertical velocity multiplier", 0.0, 0.0, 1.0));

    public AntiVelocity() { super("AntiVelocity", "Reduces knockback velocity from hits", Category.MOVEMENT); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onPacketReceive(EventPacketReceive e) {
        if (!(e.getPacket() instanceof EntityVelocityUpdateS2CPacket pkt)) return;
        if (mc.player == null || pkt.getEntityId() != mc.player.getId()) return;
        // Cancel the packet to prevent knockback
        e.cancel();
        // Apply reduced velocity
        mc.player.setVelocity(
            mc.player.getVelocity().x + (pkt.getVelocityX() / 8000.0) * hMult.get(),
            mc.player.getVelocity().y + (pkt.getVelocityY() / 8000.0) * vMult.get(),
            mc.player.getVelocity().z + (pkt.getVelocityZ() / 8000.0) * hMult.get()
        );
    }
}
