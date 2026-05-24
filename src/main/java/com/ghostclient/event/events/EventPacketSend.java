package com.ghostclient.event.events;

import com.ghostclient.event.Event;
import net.minecraft.network.packet.Packet;

/**
 * Fired before a packet is sent from the client to the server.
 * Cancel to prevent the packet from being sent.
 */
public class EventPacketSend extends Event {

    private Packet<?> packet;

    public EventPacketSend(Packet<?> packet) {
        this.packet = packet;
    }

    /**
     * The packet about to be sent.
     */
    public Packet<?> getPacket() {
        return packet;
    }

    /**
     * Replace the packet to be sent with a different one.
     */
    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }
}
