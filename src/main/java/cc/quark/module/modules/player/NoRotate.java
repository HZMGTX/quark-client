package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

/**
 * NoRotate - blocks server-forced rotation packets (LookAtS2CPacket and
 * PlayerPositionLookS2CPacket) from changing the player's view direction.
 *
 * For PlayerPositionLookS2CPacket we let the position update through
 * (so we don't desync from the server) but immediately restore our
 * own yaw/pitch on the render thread.
 */
public class NoRotate extends Module {

    private final BoolSetting cancelLookAt = register(new BoolSetting(
            "Cancel LookAt", "Cancel LookAt packets entirely", true));
    private final BoolSetting restoreOnTeleport = register(new BoolSetting(
            "Restore On Teleport", "Restore rotation after position-look packets", true));

    public NoRotate() {
        super("NoRotate", "Prevents server from forcing player rotation", Category.PLAYER);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;

        // Cancel LookAt entirely
        if (cancelLookAt.isEnabled() && event.getPacket() instanceof LookAtS2CPacket) {
            event.cancel();
            return;
        }

        // For PlayerPositionLook: allow position update but immediately restore rotation
        if (restoreOnTeleport.isEnabled() && event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            final float savedYaw   = mc.player.getYaw();
            final float savedPitch = mc.player.getPitch();

            mc.execute(() -> {
                if (mc.player == null) return;
                mc.player.setYaw(savedYaw);
                mc.player.setPitch(savedPitch);
                mc.player.prevYaw   = savedYaw;
                mc.player.prevPitch = savedPitch;
            });
        }
    }
}
