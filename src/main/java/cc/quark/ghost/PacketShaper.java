package cc.quark.ghost;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * PacketShaper - shapes outgoing packet timing and order so the stream looks
 * vanilla to server-side anti-cheat analysis.
 *
 * <p>Features:
 * <ul>
 *   <li>Queues packets with a realistic send delay.</li>
 *   <li>Adds Gaussian jitter to packet timestamps.</li>
 *   <li>Detects suspicious packet sequences (e.g. attack immediately after teleport).</li>
 *   <li>Exposes a recommended inter-packet interval tuned per active AC profile.</li>
 * </ul>
 *
 * <p>Call {@link #tick()} once per client tick to drain the queue.
 */
public class PacketShaper {

    // -------------------------------------------------------------------------
    // Internal data
    // -------------------------------------------------------------------------

    /** A packet held in the send queue with its scheduled send timestamp. */
    private static final class TimedPacket {
        final Packet<?> packet;
        final long sendAt; // System.currentTimeMillis() when this should be sent

        TimedPacket(Packet<?> packet, long sendAt) {
            this.packet = packet;
            this.sendAt = sendAt;
        }
    }

    private final Queue<TimedPacket> packetQueue = new LinkedList<>();

    /** Rolling history of the last 20 packet classes, newest first. */
    private final LinkedList<Class<?>> recentPacketClasses = new LinkedList<>();
    private static final int HISTORY_SIZE = 20;

    private final Random random = new Random();

    // -------------------------------------------------------------------------
    // Queue management
    // -------------------------------------------------------------------------

    /**
     * Schedules {@code packet} to be sent after approximately {@code delayMs}
     * milliseconds, with a small Gaussian jitter applied.
     *
     * @param packet  packet to queue
     * @param delayMs base delay before sending (ms); jitter of Â±5 ms is added
     */
    public void queuePacket(Packet<?> packet, long delayMs) {
        if (packet == null) return;
        long jitter = (long) (random.nextGaussian() * 5.0);
        long sendAt = System.currentTimeMillis() + Math.max(0L, delayMs + jitter);
        packetQueue.add(new TimedPacket(packet, sendAt));
    }

    /**
     * Drains all due packets from the queue and sends them through the network
     * handler.  Must be called once per client tick.
     */
    public void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() == null) return;

        long now = System.currentTimeMillis();

        while (!packetQueue.isEmpty()) {
            TimedPacket head = packetQueue.peek();
            if (head == null || head.sendAt > now) break;

            packetQueue.poll();
            mc.getNetworkHandler().sendPacket(head.packet);
            recordPacketClass(head.packet.getClass());
        }
    }

    // -------------------------------------------------------------------------
    // Sequence analysis
    // -------------------------------------------------------------------------

    /**
     * Records a packet class in the rolling recent-packet history.
     */
    private void recordPacketClass(Class<?> cls) {
        recentPacketClasses.addFirst(cls);
        while (recentPacketClasses.size() > HISTORY_SIZE) {
            recentPacketClasses.removeLast();
        }
    }

    /**
     * Returns {@code true} when the provided packet sequence looks suspicious
     * from an anti-cheat perspective.
     *
     * <p>Current heuristics:
     * <ul>
     *   <li>An attack packet ({@code PlayerInteractEntityC2SPacket}) appearing
     *       immediately after a teleport / position packet more than once in the
     *       recent history.</li>
     *   <li>More than 4 swing packets ({@code HandSwingC2SPacket}) without any
     *       intervening movement or position packets.</li>
     * </ul>
     *
     * @param recentPackets list of recent packet classes, newest first
     * @return true if the sequence looks suspicious
     */
    public boolean isSuspiciousSequence(List<Class<?>> recentPackets) {
        if (recentPackets == null || recentPackets.isEmpty()) return false;

        int attackAfterMove = 0;
        int consecutiveSwings = 0;
        int swingsBetweenMoves = 0;
        boolean lastWasSwing = false;

        for (int i = 0; i < recentPackets.size(); i++) {
            Class<?> cls = recentPackets.get(i);

            if (cls == PlayerInteractEntityC2SPacket.class) {
                // Check if the packet immediately before this was a position packet
                if (i + 1 < recentPackets.size() &&
                        isPositionPacket(recentPackets.get(i + 1))) {
                    attackAfterMove++;
                }
            }

            if (cls == HandSwingC2SPacket.class) {
                swingsBetweenMoves++;
                consecutiveSwings++;
                lastWasSwing = true;
            } else {
                if (isPositionPacket(cls)) {
                    swingsBetweenMoves = 0;
                }
                consecutiveSwings = 0;
                lastWasSwing = false;
            }
        }

        if (attackAfterMove >= 2) return true;
        if (swingsBetweenMoves > 4) return true;

        return false;
    }

    /**
     * Convenience overload that analyses the module's own rolling history.
     */
    public boolean isSuspiciousSequence() {
        return isSuspiciousSequence(new ArrayList<>(recentPacketClasses));
    }

    private boolean isPositionPacket(Class<?> cls) {
        return cls == PlayerMoveC2SPacket.class ||
               cls == PlayerMoveC2SPacket.PositionAndOnGround.class ||
               cls == PlayerMoveC2SPacket.Full.class ||
               cls == PlayerMoveC2SPacket.LookAndOnGround.class ||
               cls == PlayerMoveC2SPacket.OnGroundOnly.class;
    }

    // -------------------------------------------------------------------------
    // Recommended interval
    // -------------------------------------------------------------------------

    /**
     * Returns the recommended millisecond interval between outgoing packets for
     * the currently active anti-cheat profile.
     *
     * <p>Lower values are less safe (faster = more suspicious), but higher values
     * introduce visible latency in module responsiveness.
     *
     * @return recommended interval in milliseconds
     */
    public long getPacketInterval() {
        GhostManager.AntiCheatProfile profile = GhostManager.INSTANCE.getActiveProfile();
        return switch (profile) {
            case VANILLA  -> 0L;
            case NCP      -> 5L;
            case AAC      -> 8L;
            case SPARTAN  -> 8L;
            case WATCHDOG -> 10L;
            case MATRIX   -> 10L;
            case GRIM     -> 15L;
            case INTAVE   -> 12L;
            case CUSTOM   -> 10L;
        };
    }
}
