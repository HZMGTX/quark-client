package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * PingSpoofCombat — delays outgoing attack packets (PlayerInteractEntityC2SPacket)
 * by Delay milliseconds to simulate high ping.  The queued packets are released
 * after the delay expires.
 */
public class PingSpoofCombat extends Module {

    private final IntSetting delayMs = register(new IntSetting("Delay", "Outgoing attack packet delay in ms", 150, 0, 500));

    /** Pair of [packet, timestamp] */
    private final Deque<Object[]> queue = new ArrayDeque<>();

    public PingSpoofCombat() {
        super("PingSpoofCombat", "Delays outgoing attack packets to simulate ping", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        queue.clear();
    }

    @Override
    public void onDisable() {
        flushAll();
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (!(event.getPacket() instanceof PlayerInteractEntityC2SPacket)) return;

        // Cancel the immediate send and queue it
        event.cancel();
        queue.addLast(new Object[]{ event.getPacket(), System.currentTimeMillis() });

        // Release any packets whose delay has expired
        long threshold = System.currentTimeMillis() - delayMs.get();
        while (!queue.isEmpty()) {
            Object[] head = queue.peekFirst();
            if ((long) head[1] <= threshold) {
                queue.pollFirst();
                mc.getNetworkHandler().sendPacket((Packet<?>) head[0]);
            } else {
                break;
            }
        }
    }

    private void flushAll() {
        if (mc.getNetworkHandler() == null) { queue.clear(); return; }
        while (!queue.isEmpty()) {
            Object[] entry = queue.pollFirst();
            mc.getNetworkHandler().sendPacket((Packet<?>) entry[0]);
        }
    }
}
