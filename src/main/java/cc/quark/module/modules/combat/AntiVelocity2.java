package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

/**
 * AntiVelocity2 — intercepts EntityVelocityUpdateS2CPacket for the local player
 * and scales the velocity values by configurable horizontal and vertical factors.
 */
public class AntiVelocity2 extends Module {

    private final DoubleSetting horizontalFactor = register(new DoubleSetting(
            "Horizontal", "Multiplier for horizontal knockback (0 = none, 1 = full)", 0.0, 0.0, 1.0));

    private final DoubleSetting verticalFactor = register(new DoubleSetting(
            "Vertical", "Multiplier for vertical knockback (0 = none, 1 = full)", 0.0, 0.0, 1.0));

    public AntiVelocity2() {
        super("AntiVelocity2", "Reduces or cancels knockback from the server", Category.COMBAT);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet)) return;
        if (packet.getId() != mc.player.getId()) return;

        // Retrieve the velocity values from the packet via reflection since they are final
        // We cancel the packet and manually apply scaled velocity instead
        event.setCancelled(true);

        double vx = (packet.getVelocityX() / 8000.0) * horizontalFactor.get();
        double vy = (packet.getVelocityY() / 8000.0) * verticalFactor.get();
        double vz = (packet.getVelocityZ() / 8000.0) * horizontalFactor.get();

        mc.player.setVelocity(vx, vy, vz);
    }
}
