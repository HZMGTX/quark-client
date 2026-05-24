package cc.quark.event.events;

import cc.quark.event.Event;
import net.minecraft.network.packet.Packet;

/**
 * Fired when the client receives a packet from the server.
 * Cancel to prevent the client from processing the packet.
 */
public class EventPacketReceive extends Event {

    private Packet<?> packet;

    public EventPacketReceive(Packet<?> packet) {
        this.packet = packet;
    }

    /**
     * The packet received from the server.
     */
    public Packet<?> getPacket() {
        return packet;
    }

    /**
     * Replace the received packet with a different one.
     */
    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }
}
