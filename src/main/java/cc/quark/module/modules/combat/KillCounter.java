package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.ChatUtil;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class KillCounter extends Module {

    private int kills = 0;

    public KillCounter() {
        super("KillCounter", "Tracks and displays kills this session; logs total on disable", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        kills = 0;
    }

    @Override
    public void onDisable() {
        ChatUtil.addMessage("Kill Counter: " + kills + " kill(s) this session.");
    }

    @Override
    public String getSuffix() {
        return String.valueOf(kills);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        // Entity status 35 = entity death
        if (event.getPacket() instanceof EntityStatusS2CPacket pkt) {
            mc.execute(() -> {
                if (pkt.getStatus() == 35 && pkt.getEntity(mc.world) != mc.player) {
                    kills++;
                }
            });
        }
    }
}
