package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

/**
 * BlastProof - cancels or dampens incoming explosion packets from the server.
 *
 * <ul>
 *   <li><b>Cancel Packet</b>  - completely suppresses the explosion S2C packet so the
 *       client never applies the velocity or block damage from it.</li>
 *   <li><b>Reduce Velocity</b> - instead of full cancel, multiplies the explosion velocity
 *       push by a configurable factor so the player is still pushed but less severely.</li>
 * </ul>
 */
public class BlastProof extends Module {

    private final BoolSetting cancelPacket = register(new BoolSetting(
            "Cancel Packet", "Fully cancel explosion packets (no knockback, no block damage client-side)", true));

    private final BoolSetting reduceVelocity = register(new BoolSetting(
            "Reduce Velocity", "Reduce explosion knockback instead of full cancel", false));

    private final DoubleSetting velocityFactor = register(new DoubleSetting(
            "Velocity Factor", "Multiplier applied to explosion knockback (0 = none, 1 = full)", 0.2, 0.0, 1.0));

    private final BoolSetting notifyExplosion = register(new BoolSetting(
            "Notify", "Print a message when an explosion is blocked", false));

    public BlastProof() {
        super("BlastProof", "Cancels or dampens server-side explosion velocity packets", Category.COMBAT);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof ExplosionS2CPacket explosion)) return;

        if (cancelPacket.isEnabled()) {
            event.cancel();
            if (notifyExplosion.isEnabled()) {
                mc.player.sendMessage(net.minecraft.text.Text.literal(
                        "[BlastProof] Blocked explosion (radius: "
                                + String.format("%.1f", explosion.getRadius()) + ")"), false);
            }
            return;
        }

        if (reduceVelocity.isEnabled()) {
            // Apply reduced velocity factor to the player's velocity after the packet fires
            // We schedule velocity reduction in the next tick
            pendingReduction = true;
        }
    }

    // Flag used to apply velocity reduction after the explosion packet is processed
    private boolean pendingReduction = false;

    @EventHandler
    public void onTick(EventTick event) {
        if (!pendingReduction || mc.player == null) return;
        pendingReduction = false;

        double factor = velocityFactor.get();
        mc.player.setVelocity(
                mc.player.getVelocity().multiply(factor)
        );
    }
}
