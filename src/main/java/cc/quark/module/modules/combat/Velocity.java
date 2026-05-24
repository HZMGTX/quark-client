package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.EnumSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

/**
 * Velocity (AntiKnockback) - intercepts velocity packets sent by the server and
 * scales down or fully cancels the resulting knockback applied to the player.
 *
 * <p>Modes:
 * <ul>
 *   <li><b>Cancel</b> - zeroes out all knockback.</li>
 *   <li><b>Reduce</b> - scales knockback by the configured horizontal/vertical percentages.</li>
 *   <li><b>Jump</b>   - cancels horizontal knockback and makes the player jump instead.</li>
 * </ul>
 */
public class Velocity extends Module {

    public enum VelocityMode {
        CANCEL, REDUCE, JUMP
    }

    private final DoubleSetting horizontal = register(new DoubleSetting(
            "Horizontal", "Percentage of horizontal knockback to keep (0 = none)", 0.0, 0.0, 100.0));

    private final DoubleSetting vertical = register(new DoubleSetting(
            "Vertical", "Percentage of vertical knockback to keep (0 = none)", 0.0, 0.0, 100.0));

    private final EnumSetting<VelocityMode> mode = register(new EnumSetting<>(
            "Mode", "How to handle the incoming velocity packet", VelocityMode.CANCEL));

    public Velocity() {
        super("Velocity", "Reduces or cancels knockback from hits", Category.COMBAT);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet)) return;

        // Only modify packets targeting the local player
        if (packet.getEntityId() != mc.player.getId()) return;

        switch (mode.get()) {
            case CANCEL -> event.cancel();

            case REDUCE -> {
                // The packet velocities are in units of 1/8000 blocks per tick
                double hScale = horizontal.get() / 100.0;
                double vScale = vertical.get()   / 100.0;

                int velX = (int) (packet.getVelocityX() * hScale);
                int velY = (int) (packet.getVelocityY() * vScale);
                int velZ = (int) (packet.getVelocityZ() * hScale);

                // Replace packet with scaled velocity
                event.setPacket(new EntityVelocityUpdateS2CPacket(
                        packet.getEntityId(), velX, velY, velZ));
            }

            case JUMP -> {
                // Cancel horizontal knockback; replace with zero-horizontal, zero-vertical packet
                event.setPacket(new EntityVelocityUpdateS2CPacket(
                        packet.getEntityId(), 0, 0, 0));
                // Jump to visually handle the hit
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                }
            }
        }
    }
}
