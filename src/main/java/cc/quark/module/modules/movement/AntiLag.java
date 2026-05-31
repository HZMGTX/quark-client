package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class AntiLag extends Module {

    private final IntSetting packetsPerSecond = register(new IntSetting(
            "Packets Per Second", "Number of movement packets sent per second", 10, 1, 20));

    private final TimerUtil timer = new TimerUtil();

    public AntiLag() {
        super("AntiLag", "Reduces movement packet rate to prevent lag-induced desync",
                Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!(event.getPacket() instanceof PlayerMoveC2SPacket)) return;

        int pps = packetsPerSecond.get();
        long intervalMs = 1000L / pps;

        if (!timer.hasReached(intervalMs)) {
            event.cancel();
        } else {
            timer.reset();
        }
    }
}
