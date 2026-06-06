package cc.quark.module.modules.player;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;

public class AntiHurtCam extends Module {

    public AntiHurtCam() {
        super("AntiHurtCam", "Prevents the screen from tilting when taking damage", Category.PLAYER);
    }

    @Override public void onEnable()  { Quark.mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { Quark.mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onPacket(EventPacketReceive event) {
        var mc = Quark.mc;
        if (mc == null || mc.player == null) return;
        if (event.getPacket() instanceof EntityDamageS2CPacket pkt) {
            if (pkt.entityId() == mc.player.getId()) {
                // Temporarily zero the hurtTime after packet by scheduling a reset
                mc.player.hurtTime = 0;
                mc.player.hurtYaw = 0;
            }
        }
    }
}
