package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

/**
 * VelocityBoost - amplify received knockback by Multiplier (1.0-5.0); useful
 * for long-distance gliding after being hit.
 */
public class VelocityBoost extends Module {

    private final DoubleSetting multiplier = register(new DoubleSetting(
            "Multiplier", "Knockback velocity multiplier (1.0 = vanilla)", 2.0, 1.0, 5.0));
    private final DoubleSetting vertMult = register(new DoubleSetting(
            "Vertical Mult", "Separate vertical multiplier", 1.0, 0.0, 5.0));

    public VelocityBoost() {
        super("VelocityBoost", "Amplify knockback for long-distance gliding after a hit", Category.MOVEMENT);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof EntityVelocityUpdateS2CPacket pkt)) return;
        if (pkt.getEntityId() != mc.player.getId()) return;

        event.cancel();

        double hMult = multiplier.get();
        double vMult = vertMult.get();

        double velX = (pkt.getVelocityX() / 8000.0) * hMult;
        double velY = (pkt.getVelocityY() / 8000.0) * vMult;
        double velZ = (pkt.getVelocityZ() / 8000.0) * hMult;

        mc.player.setVelocity(velX, velY, velZ);
    }
}
