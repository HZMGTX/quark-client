package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

public class AntiVelocity3 extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Knockback cancellation mode", "Full", "Full", "Reduce"));

    private final IntSetting reducePercent = register(new IntSetting(
            "ReducePercent", "Percentage to reduce knockback in Reduce mode", 50, 0, 100));

    public AntiVelocity3() {
        super("AntiVelocity3", "Cancels knockback from projectiles and explosions", Category.MOVEMENT);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;

        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket pkt) {
            if (pkt.getEntityId() != mc.player.getId()) return;
            handleVelocity(event, pkt);
        } else if (event.getPacket() instanceof ExplosionS2CPacket) {
            event.cancel();
        }
    }

    private void handleVelocity(EventPacketReceive event, EntityVelocityUpdateS2CPacket pkt) {
        if (mode.get().equals("Full")) {
            event.cancel();
        } else {
            event.cancel();
            double factor = 1.0 - (reducePercent.get() / 100.0);
            double vx = (pkt.getVelocityX() / 8000.0) * factor;
            double vy = (pkt.getVelocityY() / 8000.0) * factor;
            double vz = (pkt.getVelocityZ() / 8000.0) * factor;
            mc.player.setVelocity(vx, vy, vz);
        }
    }
}
