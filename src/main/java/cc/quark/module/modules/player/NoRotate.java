package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

/**
 * NoRotate - blocks server-forced rotation packets.
 *
 * Servers can force the player to look in a direction via PlayerPositionLookS2CPacket
 * (which includes yaw/pitch) and LookAtS2CPacket. This module cancels both,
 * then re-sends the position without the rotation change to stay in sync.
 */
public class NoRotate extends Module {

    public NoRotate() {
        super("NoRotate", "Prevents server from forcing player rotation", Category.PLAYER);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;

        if (event.getPacket() instanceof LookAtS2CPacket) {
            // Completely cancel look-at commands
            event.cancel();
            return;
        }

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket pkt) {
            // Allow the position update but restore our own yaw/pitch immediately after
            // We do this by letting the packet through but scheduling a rotation restore
            float savedYaw = mc.player.getYaw();
            float savedPitch = mc.player.getPitch();

            // The packet will apply, so we schedule restoration on the next tick.
            // We use a Runnable queued to the render thread.
            final float yawToRestore = savedYaw;
            final float pitchToRestore = savedPitch;

            mc.execute(() -> {
                if (mc.player != null) {
                    mc.player.setYaw(yawToRestore);
                    mc.player.setPitch(pitchToRestore);
                    // Also restore the renderer's camera rotations
                    mc.player.prevYaw = yawToRestore;
                    mc.player.prevPitch = pitchToRestore;
                }
            });
        }
    }
}
