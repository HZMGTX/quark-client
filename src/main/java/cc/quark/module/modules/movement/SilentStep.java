package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class SilentStep extends Module {

    private final BoolSetting stopParticles = register(new BoolSetting(
            "StopParticles", "Cancel step-triggered particle packets sent to server", true));

    public SilentStep() {
        super("SilentStep", "Stops footstep sounds from being sent to server", Category.MOVEMENT);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;

        // Intercept position packets where the player just landed (on-ground transition)
        // to prevent step sound triggers from reaching the server
        if (event.getPacket() instanceof PlayerMoveC2SPacket pkt) {
            if (pkt.isOnGround() && stopParticles.isEnabled()) {
                // Replace with a spoofed packet that reports not-on-ground briefly
                // to suppress the server-side footstep event
                event.setPacket(new PlayerMoveC2SPacket.Full(
                        pkt.getX(mc.player.getX()),
                        pkt.getY(mc.player.getY()),
                        pkt.getZ(mc.player.getZ()),
                        mc.player.getYaw(),
                        mc.player.getPitch(),
                        false
                ));
            }
        }
    }
}
