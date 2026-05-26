package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.EnumSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;

public class Velocity extends Module {

    public enum VelocityMode {
        REDUCE, ZERO, REVERSE, JUMP
    }

    private final EnumSetting<VelocityMode> mode = register(new EnumSetting<>(
            "Mode", "How to handle the incoming velocity packet", VelocityMode.REDUCE));

    private final BoolSetting horizontal = register(new BoolSetting(
            "Horizontal", "Apply reduction to horizontal knockback", true));

    private final BoolSetting vertical = register(new BoolSetting(
            "Vertical", "Apply reduction to vertical knockback", true));

    private final DoubleSetting horizontalPct = register(new DoubleSetting(
            "Horizontal %", "Percentage of horizontal knockback to remove (100 = none)", 100.0, 0.0, 100.0));

    private final DoubleSetting verticalPct = register(new DoubleSetting(
            "Vertical %", "Percentage of vertical knockback to remove (100 = none)", 100.0, 0.0, 100.0));

    public Velocity() {
        super("Velocity", "Reduces or cancels knockback from hits", Category.COMBAT);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet)) return;

        if (packet.getEntityId() != mc.player.getId()) return;

        double velX = packet.getVelocityX() / 8000.0;
        double velY = packet.getVelocityY() / 8000.0;
        double velZ = packet.getVelocityZ() / 8000.0;

        switch (mode.get()) {
            case ZERO -> {
                double newX = horizontal.isEnabled() ? 0.0 : velX;
                double newY = vertical.isEnabled() ? 0.0 : velY;
                double newZ = horizontal.isEnabled() ? 0.0 : velZ;
                event.setPacket(new EntityVelocityUpdateS2CPacket(
                        packet.getEntityId(), new Vec3d(newX, newY, newZ)));
            }
            case REDUCE -> {
                double hScale = horizontal.isEnabled() ? (1.0 - horizontalPct.get() / 100.0) : 1.0;
                double vScale = vertical.isEnabled() ? (1.0 - verticalPct.get() / 100.0) : 1.0;
                event.setPacket(new EntityVelocityUpdateS2CPacket(
                        packet.getEntityId(), new Vec3d(velX * hScale, velY * vScale, velZ * hScale)));
            }
            case REVERSE -> {
                double newX = horizontal.isEnabled() ? -velX : velX;
                double newY = vertical.isEnabled() ? -velY : velY;
                double newZ = horizontal.isEnabled() ? -velZ : velZ;
                event.setPacket(new EntityVelocityUpdateS2CPacket(
                        packet.getEntityId(), new Vec3d(newX, newY, newZ)));
            }
            case JUMP -> {
                event.setPacket(new EntityVelocityUpdateS2CPacket(
                        packet.getEntityId(), Vec3d.ZERO));
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                }
            }
        }
    }
}
