package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;

public class AntiHungerLoss extends Module {

    public AntiHungerLoss() {
        super("AntiHungerLoss", "Stops hunger from decreasing", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        var hunger = mc.player.getHungerManager();
        if (hunger.getFoodLevel() < 20) {
            hunger.setFoodLevel(20);
        }
        if (hunger.getSaturationLevel() < 5.0f) {
            hunger.setSaturationLevel(5.0f);
        }
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (event.getPacket() instanceof HealthUpdateS2CPacket pkt) {
            event.setPacket(new HealthUpdateS2CPacket(pkt.getHealth(), 20, 5.0f));
        }
    }
}
