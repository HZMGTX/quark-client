package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

public class ZeroVelocity extends Module {

    private final BoolSetting onlyKnockback = register(new BoolSetting(
            "OnlyKnockback", "Only cancel knockback velocity; allow other velocity changes",
            false));

    public ZeroVelocity() {
        super("ZeroVelocity", "Completely cancels all incoming velocity from any source",
                Category.MOVEMENT);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;

        var pkt = event.getPacket();

        if (pkt instanceof EntityVelocityUpdateS2CPacket velPkt) {
            if (velPkt.getEntityId() == mc.player.getId()) {
                event.cancel();
            }
            return;
        }

        if (!onlyKnockback.isEnabled() && pkt instanceof ExplosionS2CPacket) {
            event.cancel();
        }
    }
}
