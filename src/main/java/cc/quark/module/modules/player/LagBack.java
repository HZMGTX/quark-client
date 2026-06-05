package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class LagBack extends Module {
    private final BoolSetting stopOnLag = register(new BoolSetting("Stop On Lag", "Stop moving when lag detected", true));
    private boolean lagging = false;
    private final TimerUtil lagTimer = new TimerUtil();

    public LagBack() { super("LagBack", "Detects lag-backs and optionally stops movement", Category.PLAYER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); lagging = false; }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onPacketReceive(EventPacketReceive e) {
        if (!(e.getPacket() instanceof PlayerPositionLookS2CPacket)) return;
        lagging = true;
        lagTimer.reset();
        ChatUtil.warn("Lag-back detected!");
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (lagTimer.hasReached(2000)) lagging = false;
        if (lagging && stopOnLag.isEnabled() && mc.player != null) {
            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
        }
    }
}
