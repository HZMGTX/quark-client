package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

/**
 * PacketReach - extends effective attack range by sending position packets
 * slightly ahead of the player in the direction they are looking before the
 * movement packet is processed by the server.
 */
public class PacketReach extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Extra reach distance in blocks", 4.2, 3.0, 6.0));

    private final IntSetting packets = register(new IntSetting(
            "Packets", "Number of position packets to send per tick", 2, 1, 5));

    public PacketReach() {
        super("PacketReach", "Extends reach by spoofing position packets", Category.COMBAT);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        double x = event.getX();
        double y = event.getY();
        double z = event.getZ();
        float yaw = event.getYaw();

        // Compute look direction (horizontal)
        double radYaw = Math.toRadians(yaw);
        double dx = -Math.sin(radYaw);
        double dz  = Math.cos(radYaw);

        double extraReach = range.get() - 3.0; // vanilla reach is ~3 blocks
        int count = packets.get();

        for (int i = 1; i <= count; i++) {
            double step = (extraReach / count) * i;
            mc.getNetworkHandler().sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(
                            x + dx * step,
                            y,
                            z + dz * step,
                            event.isOnGround()));
        }

        // Reset to real position so server does not flag us for teleporting
        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, event.isOnGround()));
    }
}
