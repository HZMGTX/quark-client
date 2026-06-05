package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

public class Velocity extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "How to handle incoming velocity from knockback",
            "Reduce", "Reduce", "Zero", "Reverse"));

    private final DoubleSetting horizontal = register(new DoubleSetting(
            "Horizontal", "Horizontal knockback multiplier percentage (0 = none, 100 = full)", 0.0, 0.0, 200.0));

    private final DoubleSetting vertical = register(new DoubleSetting(
            "Vertical", "Vertical knockback multiplier percentage (0 = none, 100 = full)", 0.0, 0.0, 200.0));

    public Velocity() {
        super("Velocity", "Reduces, cancels, or reverses knockback from hits", Category.COMBAT);
    }

    @Override
    public String getSuffix() {
        return mode.get() + " " + (int) horizontal.get() + "%";
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof EntityVelocityUpdateS2CPacket pkt)) return;
        if (pkt.getEntityId() != mc.player.getId()) return;

        // Cancel the original packet so vanilla code doesn't apply it
        event.cancel();

        double velX = pkt.getVelocityX() / 8000.0;
        double velY = pkt.getVelocityY() / 8000.0;
        double velZ = pkt.getVelocityZ() / 8000.0;

        double hScale = horizontal.get() / 100.0;
        double vScale = vertical.get() / 100.0;

        double newX, newY, newZ;

        switch (mode.get()) {
            case "Zero" -> {
                newX = 0.0;
                newY = 0.0;
                newZ = 0.0;
            }
            case "Reverse" -> {
                newX = -velX * hScale;
                newY = -velY * vScale;
                newZ = -velZ * hScale;
            }
            default -> {
                // Reduce: multiply by the configured percentage
                newX = velX * hScale;
                newY = velY * vScale;
                newZ = velZ * hScale;
            }
        }

        mc.player.setVelocity(newX, newY, newZ);
    }
}
