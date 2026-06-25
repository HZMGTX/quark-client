package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;

public class AntiSleep extends Module {

    private final BoolSetting alertOnForce = register(new BoolSetting(
            "AlertOnForce", "Notify in chat when a forced sleep is cancelled", true));

    public AntiSleep() {
        super("AntiSleep", "Prevents forced sleep by cancelling the sleep packet", Category.PLAYER);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof GameStateChangeS2CPacket pkt)) return;
        if (pkt.getReason() == GameStateChangeS2CPacket.ENTER_CREDITS) return;

        mc.execute(() -> {
            if (mc.player == null) return;
            if (mc.player.isSleeping()) {
                mc.player.wakeUp();
                if (alertOnForce.isEnabled()) {
                    ChatUtil.warn("[AntiSleep] Cancelled forced sleep");
                }
            }
        });
    }
}
