package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.ArrayDeque;
import java.util.Deque;

public class Blink extends Module {

    private final IntSetting maxPackets = register(new IntSetting(
            "Max Packets", "Auto-flush after this many queued packets (0 = manual)", 100, 0, 500));

    private final Deque<Packet<?>> queue = new ArrayDeque<>();

    public Blink() {
        super("Blink", "Buffers movement packets then sends them all at once on disable", Category.MISC);
    }

    @Override
    public void onEnable() {
        queue.clear();
    }

    @Override
    public void onDisable() {
        flush();
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!(event.getPacket() instanceof PlayerMoveC2SPacket)) return;
        event.cancel();
        queue.add(event.getPacket());

        int max = maxPackets.get();
        if (max > 0 && queue.size() >= max) flush();
    }

    private void flush() {
        if (mc.getNetworkHandler() == null) { queue.clear(); return; }
        while (!queue.isEmpty()) {
            mc.getNetworkHandler().sendPacket(queue.poll());
        }
    }

    public int getQueuedCount() { return queue.size(); }
}
