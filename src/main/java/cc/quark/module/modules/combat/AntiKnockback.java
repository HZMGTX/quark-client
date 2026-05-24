package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

/**
 * AntiKnockback â€” reduces or negates knockback received from hits.
 *
 * <p>Intercepts incoming velocity-update packets from the server and scales
 * down the velocity values so the player is knocked back less.
 */
public class AntiKnockback extends Module {

    private final DoubleSetting horizontal;
    private final DoubleSetting vertical;

    public AntiKnockback() {
        super("AntiKnockback", "Reduces knockback received from attacks.", Category.COMBAT);

        horizontal = doubleSetting("Horizontal", "Horizontal knockback multiplier (0 = none).", 0.0, 0.0, 1.0);
        vertical   = doubleSetting("Vertical",   "Vertical knockback multiplier (0 = none).",   0.0, 0.0, 1.0);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof EntityVelocityUpdateS2CPacket pkt)) return;

        // Only apply to packets targeting the local player
        if (pkt.getId() != mc.player.getId()) return;

        // Cancel to completely suppress knockback when both multipliers are zero
        if (horizontal.get() == 0.0 && vertical.get() == 0.0) {
            event.cancel();
            return;
        }

        // Partial knockback: we need to cancel and re-apply scaled velocity manually.
        // Since the packet is immutable in 1.20.4, we cancel it and set velocity directly.
        event.cancel();

        double velX = pkt.getVelocityX() / 8000.0 * horizontal.get();
        double velY = pkt.getVelocityY() / 8000.0 * vertical.get();
        double velZ = pkt.getVelocityZ() / 8000.0 * horizontal.get();

        mc.player.setVelocity(velX, velY, velZ);
    }
}
