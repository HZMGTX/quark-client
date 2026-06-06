package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.AbstractMap;
import java.util.Map;

public class PingSpoof extends Module {

    private final IntSetting targetMs = register(new IntSetting("Target MS", "Simulated latency in milliseconds", 100, 50, 500));

    private final Deque<Map.Entry<Long, net.minecraft.network.packet.Packet<?>>> queue = new ArrayDeque<>();

    public PingSpoof() {
        super("PingSpoof", "Delays packet sending to simulate a target ping", Category.MISC);
    }

    @Override
    public void onDisable() {
        flushAll();
    }

    @EventHandler
    public void onPacketSend(EventPacketSend e) {
        if (mc.getNetworkHandler() == null) return;
        long now = System.currentTimeMillis();
        queue.addLast(new AbstractMap.SimpleEntry<>(now, e.getPacket()));
        e.cancel();

        long threshold = now - targetMs.get();
        while (!queue.isEmpty() && queue.peekFirst().getKey() <= threshold) {
            var entry = queue.pollFirst();
            try {
                mc.getNetworkHandler().getConnection().send(entry.getValue());
            } catch (Exception ignored) {}
        }
    }

    private void flushAll() {
        if (mc.getNetworkHandler() == null) { queue.clear(); return; }
        while (!queue.isEmpty()) {
            try {
                mc.getNetworkHandler().getConnection().send(queue.pollFirst().getValue());
            } catch (Exception ignored) {}
        }
    }
}
