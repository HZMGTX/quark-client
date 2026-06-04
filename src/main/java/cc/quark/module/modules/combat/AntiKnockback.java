package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

/**
 * AntiKnockback — intercepts EntityVelocityUpdateS2CPacket and scales the
 * horizontal and vertical components independently.
 * 0 % = fully cancelled; 100 % = vanilla knockback.
 */
public class AntiKnockback extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "How knockback is modified", "Reduce", "Reduce", "Zero", "Reverse"));

    private final DoubleSetting horizontal = register(new DoubleSetting(
            "Horizontal", "H-knockback to keep (0 = none, 100 = full)", 0.0, 0.0, 100.0));

    private final DoubleSetting vertical = register(new DoubleSetting(
            "Vertical", "V-knockback to keep (0 = none, 100 = full)", 0.0, 0.0, 100.0));

    public AntiKnockback() {
        super("AntiKnockback", "Reduces or cancels incoming knockback", Category.COMBAT);
    }

    @Override
    public String getSuffix() {
        return mode.get() + " " + (int) horizontal.get() + "%";
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof EntityVelocityUpdateS2CPacket pkt)) return;
        if (pkt.id() != mc.player.getId()) return;

        event.cancel();

        double rawX = pkt.velocityX() / 8000.0;
        double rawY = pkt.velocityY() / 8000.0;
        double rawZ = pkt.velocityZ() / 8000.0;

        double newX, newY, newZ;
        switch (mode.get()) {
            case "Zero" -> { newX = 0; newY = 0; newZ = 0; }
            case "Reverse" -> {
                double hScale = horizontal.get() / 100.0;
                double vScale = vertical.get() / 100.0;
                newX = -rawX * hScale;
                newY = -rawY * vScale;
                newZ = -rawZ * hScale;
            }
            default -> { // Reduce
                double hScale = horizontal.get() / 100.0;
                double vScale = vertical.get() / 100.0;
                newX = rawX * hScale;
                newY = rawY * vScale;
                newZ = rawZ * hScale;
            }
        }

        mc.player.setVelocity(newX, newY, newZ);
    }
}
