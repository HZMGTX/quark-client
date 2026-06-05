package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * NoFallDamage3 - prevents fall damage by intercepting PlayerMoveC2SPackets
 * with a falling trajectory and replacing them with an OnGroundOnly packet
 * that has onGround=true. Different from NoFallDamage2 which uses EventPreMotion.
 */
public class NoFallDamage3 extends Module {

    public NoFallDamage3() {
        super("NoFallDamage3", "Prevents fall damage via packet-level onGround spoofing on EventPacketSend", Category.MOVEMENT);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof PlayerMoveC2SPacket packet)) return;

        // Only intervene when the player is falling fast enough to take damage
        if (mc.player.getVelocity().y >= -2.0) return;
        if (mc.player.fallDistance < 3.0) return;

        // Replace packet with an OnGroundOnly packet indicating the player is on the ground
        event.setPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        mc.player.fallDistance = 0;
    }
}
