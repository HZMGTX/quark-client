package com.ghostclient.mixin;

import com.ghostclient.GhostClient;
import com.ghostclient.event.events.EventPacketReceive;
import com.ghostclient.event.events.EventPacketSend;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V",
            at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        if (GhostClient.getInstance() == null) return;
        EventPacketSend event = new EventPacketSend(packet);
        GhostClient.getInstance().getEventBus().post(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "channelRead0",
            at = @At("HEAD"), cancellable = true)
    private void onChannelRead(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        if (GhostClient.getInstance() == null) return;
        EventPacketReceive event = new EventPacketReceive(packet);
        GhostClient.getInstance().getEventBus().post(event);
        if (event.isCancelled()) ci.cancel();
    }
}
