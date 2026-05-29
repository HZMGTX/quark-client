package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

/**
 * AntiKnockback2 - intercepts EntityVelocityUpdateS2CPacket and multiplies
 * horizontal and vertical components by configurable percentages.
 *
 * <p>Setting H or V to 0 cancels that axis entirely; 100 = full vanilla knockback.
 */
public class AntiKnockback2 extends Module {

    private final DoubleSetting horizontal = register(new DoubleSetting(
            "Horizontal", "Percentage of horizontal knockback to keep (0=none, 100=full)", 0.0, 0.0, 100.0));
    private final DoubleSetting vertical = register(new DoubleSetting(
            "Vertical", "Percentage of vertical knockback to keep (0=none, 100=full)", 0.0, 0.0, 100.0));

    public AntiKnockback2() {
        super("AntiKnockback2", "Reduce/cancel incoming knockback velocity packets", Category.MOVEMENT);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof EntityVelocityUpdateS2CPacket pkt)) return;
        if (pkt.getEntityId() != mc.player.getId()) return;

        // Cancel the original packet so vanilla processing is skipped
        event.cancel();

        double hMult = horizontal.get() / 100.0;
        double vMult = vertical.get()   / 100.0;

        double velX = (pkt.getVelocityX() / 8000.0) * hMult;
        double velY = (pkt.getVelocityY() / 8000.0) * vMult;
        double velZ = (pkt.getVelocityZ() / 8000.0) * hMult;

        mc.player.setVelocity(velX, velY, velZ);
    }
}
