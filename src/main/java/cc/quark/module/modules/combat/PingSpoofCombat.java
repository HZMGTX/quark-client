package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

/**
 * PingSpoofCombat - delays incoming velocity updates to simulate lag in combat.
 */
public class PingSpoofCombat extends Module {

    private final IntSetting delay = register(new IntSetting("Delay", "Spoofed delay in ticks", 5, 0, 40));

    private int counter;

    public PingSpoofCombat() {
        super("PingSpoofCombat", "Delays incoming velocity updates in combat", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        counter = 0;
    }

    @EventHandler
    public void onReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket) {
            counter++;
            if (counter % (delay.get() + 1) != 0) {
                event.cancel();
            }
        }
    }
}
