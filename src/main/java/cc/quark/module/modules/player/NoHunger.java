package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;

public class NoHunger extends Module {

    private final BoolSetting keepSaturation = register(new BoolSetting(
            "Keep Saturation", "Also keep saturation at max level", true));
    private final IntSetting foodLevel = register(new IntSetting(
            "Food Level", "Food level to maintain", 20, 1, 20));

    public NoHunger() {
        super("NoHunger", "Prevents hunger and saturation from decreasing", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        var hunger = mc.player.getHungerManager();
        if (hunger.getFoodLevel() < foodLevel.get()) {
            hunger.setFoodLevel(foodLevel.get());
        }
        if (keepSaturation.isEnabled() && hunger.getSaturationLevel() < 5.0f) {
            hunger.setSaturationLevel(5.0f);
        }
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (event.getPacket() instanceof HealthUpdateS2CPacket pkt) {
            int food = Math.max(pkt.getFood(), foodLevel.get());
            float sat = keepSaturation.isEnabled() ? Math.max(pkt.getSaturation(), 5.0f) : pkt.getSaturation();
            event.setPacket(new HealthUpdateS2CPacket(pkt.getHealth(), food, sat));
        }
    }
}
