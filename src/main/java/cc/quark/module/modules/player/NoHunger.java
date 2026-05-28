package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;

public class NoHunger extends Module {

    private final BoolSetting keepSaturation = register(new BoolSetting("Keep Saturation", "Also keep saturation at max level", true));

    public NoHunger() {
        super("NoHunger", "Prevents hunger and saturation loss", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        var hunger = mc.player.getHungerManager();
        if (hunger.getFoodLevel() < 20) {
            hunger.setFoodLevel(20);
        }
        if (keepSaturation.isEnabled() && hunger.getSaturationLevel() < 5.0f) {
            hunger.setSaturationLevel(5.0f);
        }
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (event.getPacket() instanceof HealthUpdateS2CPacket pkt) {
            float health = pkt.getHealth();
            int food = 20;
            float saturation = keepSaturation.isEnabled() ? 5.0f : pkt.getSaturation();
            event.setPacket(new HealthUpdateS2CPacket(health, food, saturation));
        }
    }
}
