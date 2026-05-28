package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

public class VelocityBoost extends Module {

    private final DoubleSetting multiplier = register(new DoubleSetting("Multiplier", "Knockback velocity multiplier", 2.0, 1.0, 5.0));

    public VelocityBoost() {
        super("VelocityBoost", "Multiply knockback velocity for long-distance gliding after a hit", Category.MOVEMENT);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof EntityVelocityUpdateS2CPacket pkt)) return;
        if (pkt.getEntityId() != mc.player.getId()) return;

        event.cancel();

        double mult = multiplier.get();
        double velX = (pkt.getVelocityX() / 8000.0) * mult;
        double velY = (pkt.getVelocityY() / 8000.0) * mult;
        double velZ = (pkt.getVelocityZ() / 8000.0) * mult;

        mc.player.setVelocity(velX, velY, velZ);
    }
}
