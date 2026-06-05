package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

/**
 * Velocity2 - intercept EntityVelocityUpdateS2CPacket for the local player and
 * multiply H (horizontal) and V (vertical) axes by the configured percent.
 * 0% = cancel all knockback; 100% = full vanilla knockback.
 */
public class Velocity2 extends Module {

    private final IntSetting horizontal = register(new IntSetting(
            "Horizontal", "Horizontal knockback percent (0 = none, 100 = full)", 0, 0, 200));
    private final IntSetting vertical = register(new IntSetting(
            "Vertical", "Vertical knockback percent (0 = none, 100 = full)", 0, 0, 200));

    public Velocity2() {
        super("Velocity2", "Scale incoming knockback H/V axes by percent", Category.MOVEMENT);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof EntityVelocityUpdateS2CPacket pkt)) return;
        if (pkt.getEntityId() != mc.player.getId()) return;

        event.cancel();

        double hMult = horizontal.get() / 100.0;
        double vMult = vertical.get()   / 100.0;

        double velX = (pkt.getVelocityX() / 8000.0) * hMult;
        double velY = (pkt.getVelocityY() / 8000.0) * vMult;
        double velZ = (pkt.getVelocityZ() / 8000.0) * hMult;

        mc.player.setVelocity(velX, velY, velZ);
    }
}
