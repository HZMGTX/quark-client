package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;

public class AntiHunger extends Module {
    private final IntSetting minFood = register(new IntSetting("Min Food", "Cancel packets below this food", 10, 1, 20));

    public AntiHunger() { super("AntiHunger", "Reduces server-side hunger drain", Category.PLAYER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onPacketReceive(EventPacketReceive e) {
        if (!(e.getPacket() instanceof HealthUpdateS2CPacket pkt)) return;
        if (pkt.food() < minFood.get() && mc.player != null && mc.player.getHungerManager().getFoodLevel() > pkt.food()) {
            e.cancel();
        }
    }
}
