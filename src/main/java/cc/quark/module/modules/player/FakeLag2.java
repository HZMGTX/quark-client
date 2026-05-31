package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.network.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class FakeLag2 extends Module {

    private final IntSetting lagMs = register(new IntSetting(
            "LagMs", "Milliseconds to hold packets before releasing them", 500, 50, 3000));

    private final List<Packet<?>> heldPackets = new ArrayList<>();
    private final TimerUtil timer = new TimerUtil();

    public FakeLag2() {
        super("FakeLag2", "Simulates lag by holding packets for a brief period then releasing", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        heldPackets.clear();
        timer.reset();
    }

    @Override
    public void onDisable() {
        flush();
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (timer.hasReached(lagMs.get())) {
            flush();
            timer.reset();
        } else {
            heldPackets.add(event.getPacket());
            event.cancel();
        }
    }

    private void flush() {
        if (mc.getNetworkHandler() == null) {
            heldPackets.clear();
            return;
        }
        for (Packet<?> pkt : heldPackets) {
            mc.getNetworkHandler().sendPacket(pkt);
        }
        heldPackets.clear();
    }
}
