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
import java.util.Random;

public class PacketShaper {

    private static final int MAX_PACKETS_PER_TICK = 5;
    private static final int HISTORY_SIZE = 40;
    private static final int PATTERN_WINDOW = 10;

    private static final class TimedPacket {
        final Packet<?> packet;
        final long sendAt;

        TimedPacket(Packet<?> packet, long sendAt) {
            this.packet = packet;
            this.sendAt = sendAt;
        }
    }

    private final LinkedList<TimedPacket> packetQueue = new LinkedList<>();
    private final LinkedList<Class<?>> recentPacketClasses = new LinkedList<>();
    private final LinkedList<Long> recentSendTimes = new LinkedList<>();
    private final Random random = new Random(System.currentTimeMillis());

    private int packetsThisTick = 0;
    private long lastTickTime   = 0L;
    private long lastJitterAddedAt = 0L;

    public void queue(Packet<?> pkt, long delayMs) {
        if (pkt == null) return;
        long jitter = (long)(random.nextGaussian() * 5.0);
        long sendAt = System.currentTimeMillis() + Math.max(0L, delayMs + jitter);
        packetQueue.add(new TimedPacket(pkt, sendAt));
    }

    public void queuePacket(Packet<?> packet, long delayMs) {
        queue(packet, delayMs);
    }

    public void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() == null) return;

        long now = System.currentTimeMillis();
        if (now - lastTickTime < 40L) {
            packetsThisTick = 0;
        }
        lastTickTime = now;
        packetsThisTick = 0;

        boolean suspiciousPattern = hasRepeatingPattern();

        while (!packetQueue.isEmpty() && packetsThisTick < MAX_PACKETS_PER_TICK) {
            TimedPacket head = packetQueue.peek();
            if (head == null || head.sendAt > now) break;

            if (suspiciousPattern && random.nextInt(4) == 0) {
                long extraDelay = 20L + (long)(random.nextGaussian() * 8.0);
                TimedPacket delayed = new TimedPacket(head.packet, now + Math.max(5L, extraDelay));
                packetQueue.poll();
                packetQueue.addFirst(delayed);
                break;
            }

            packetQueue.poll();
            mc.getNetworkHandler().sendPacket(head.packet);
            recordPacketClass(head.packet.getClass());
            recentSendTimes.addLast(now);
            if (recentSendTimes.size() > HISTORY_SIZE) recentSendTimes.removeFirst();
            packetsThisTick++;
        }
    }

    private boolean hasRepeatingPattern() {
        if (recentPacketClasses.size() < PATTERN_WINDOW * 2) return false;

        List<Class<?>> recent = new ArrayList<>(recentPacketClasses);
        int halfLen = PATTERN_WINDOW;

        boolean matches = true;
        for (int i = 0; i < halfLen; i++) {
            if (i >= recent.size() || i + halfLen >= recent.size()) {
                matches = false;
                break;
            }
            if (!recent.get(i).equals(recent.get(i + halfLen))) {
                matches = false;
                break;
            }
        }
        return matches;
    }

    private void recordPacketClass(Class<?> cls) {
        recentPacketClasses.addFirst(cls);
        while (recentPacketClasses.size() > HISTORY_SIZE) {
            recentPacketClasses.removeLast();
        }
    }

    public int getQueueSize() {
        return packetQueue.size();
    }

    public boolean isSuspiciousSequence(List<Class<?>> recentPackets) {
        if (recentPackets == null || recentPackets.isEmpty()) return false;

        int attackAfterMove  = 0;
        int swingsBetweenMoves = 0;

        for (int i = 0; i < recentPackets.size(); i++) {
            Class<?> cls = recentPackets.get(i);

            if (cls == PlayerInteractEntityC2SPacket.class) {
                if (i + 1 < recentPackets.size() && isPositionPacket(recentPackets.get(i + 1))) {
                    attackAfterMove++;
                }
            }

            if (cls == HandSwingC2SPacket.class) {
                swingsBetweenMoves++;
            } else if (isPositionPacket(cls)) {
                swingsBetweenMoves = 0;
            }
        }

        if (attackAfterMove >= 2)    return true;
        if (swingsBetweenMoves > 4)  return true;

        return false;
    }

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
            case POLAR    -> 11L;
            case VERUS    -> 13L;
            case KARHU    -> 14L;
            case CUSTOM   -> 10L;
        };
    }
}
