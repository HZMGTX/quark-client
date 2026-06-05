package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class RubberBand extends Module {
    private final BoolSetting notify = register(new BoolSetting("Notify", "Notify in chat when rubberbanded", true));
    private final BoolSetting compensate = register(new BoolSetting("Compensate", "Try to compensate rubberbanding", false));
    private int rbCount = 0;

    public RubberBand() { super("RubberBand", "Detects and handles server rubberbanding", Category.MOVEMENT); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); rbCount = 0; }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onPacketReceive(EventPacketReceive e) {
        if (!(e.getPacket() instanceof PlayerPositionLookS2CPacket)) return;
        rbCount++;
        if (notify.isEnabled()) ChatUtil.warn("Rubberbanded! (" + rbCount + "x total)");
    }

    @EventHandler
    public void onTick(EventTick e) { /* Could add compensation logic here */ }
}
