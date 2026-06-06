package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.ArrayDeque;
import java.util.AbstractMap;
import java.util.Deque;
import java.util.Map;

public class PingSpoof extends Module {

    private final IntSetting targetMs = register(new IntSetting("Target MS", "Simulated latency in milliseconds", 100, 50, 500));

    private final Deque<Map.Entry<Long, Packet<?>>> queue = new ArrayDeque<>();

    public PingSpoof() {
        super("PingSpoof", "Delays packet sending to simulate a target ping", Category.MISC);
    }

    @Override
    public void onDisable() {
        if (mc.getNetworkHandler() != null) {
            while (!queue.isEmpty()) mc.getNetworkHandler().sendPacket(queue.pollFirst().getValue());
        }
        queue.clear();
    }

    @EventHandler
    public void onPacketSend(EventPacketSend e) {
        if (mc.getNetworkHandler() == null) return;
        if (!(e.getPacket() instanceof PlayerMoveC2SPacket)) return;

        long now = System.currentTimeMillis();
        queue.addLast(new AbstractMap.SimpleEntry<>(now, e.getPacket()));
        e.cancel();

        long threshold = now - targetMs.get();
        while (!queue.isEmpty() && queue.peekFirst().getKey() <= threshold) {
            mc.getNetworkHandler().sendPacket(queue.pollFirst().getValue());
        }
    }
}
