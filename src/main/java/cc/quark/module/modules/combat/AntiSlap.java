package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class AntiSlap extends Module {

    public AntiSlap() {
        super("AntiSlap", "Cancels knockback from fishing rod hits (EntityStatus id 37)", Category.COMBAT);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof EntityStatusS2CPacket pkt)) return;
        if (mc.player == null) return;

        if (pkt.getStatus() == 37 && pkt.getEntity(mc.world) == mc.player) {
            mc.execute(() -> {
                if (mc.player == null) return;
                mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y, mc.player.getVelocity().z);
                mc.player.velocityModified = false;
            });
            event.cancel();
        }
    }
}
