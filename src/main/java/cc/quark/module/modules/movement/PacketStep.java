package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

/**
 * PacketStep - steps up blocks instantly using packet tricks.
 * When the player is about to collide horizontally with a step-able block,
 * a Y+height packet is injected before the normal movement packet to make
 * the server believe the player stepped over it.
 */
public class PacketStep extends Module {

    private final DoubleSetting height = register(new DoubleSetting(
            "Height", "Step height in blocks", 1.0, 1.0, 3.0));

    private boolean injectedStep = false;

    public PacketStep() {
        super("PacketStep", "Step up blocks instantly using packet tricks", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        injectedStep = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        // Detect horizontal collision while on/near ground — classic step scenario
        if (mc.player.horizontalCollision && mc.player.isOnGround()) {
            Vec3d pos = mc.player.getPos();
            double h = height.get();

            // Inject an elevated position packet first, then normal ground packet
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    pos.x, pos.y + h, pos.z, false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    pos.x, pos.y + h, pos.z, true));

            // Locally teleport the player upward
            mc.player.setPos(pos.x, pos.y + h, pos.z);
            mc.player.setVelocity(mc.player.getVelocity().x, 0, mc.player.getVelocity().z);
            injectedStep = true;
        } else {
            injectedStep = false;
        }
    }
}
