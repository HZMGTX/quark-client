package cc.quark.mixin;

import cc.quark.Quark;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventPacketSend;
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
    private void onSendPacketInject(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        if (Quark.getInstance() == null) return;
        // Only handle cancellation here
        EventPacketSend event = new EventPacketSend(packet);
        Quark.getInstance().getEventBus().post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @org.spongepowered.asm.mixin.injection.ModifyVariable(
            method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V",
            at = @At("HEAD"), argsOnly = true)
    private Packet<?> onSendPacketModify(Packet<?> packet) {
        if (Quark.getInstance() == null) return packet;
        EventPacketSend event = new EventPacketSend(packet);
        Quark.getInstance().getEventBus().post(event);
        // Note: isCancelled is handled by the Inject mixin above.
        return event.getPacket() != null ? event.getPacket() : packet;
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V",
            at = @At("HEAD"), cancellable = true)
    private void onChannelReadInject(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        if (Quark.getInstance() == null) return;
        EventPacketReceive event = new EventPacketReceive(packet);
        Quark.getInstance().getEventBus().post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @org.spongepowered.asm.mixin.injection.ModifyVariable(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V",
            at = @At("HEAD"), argsOnly = true)
    private Packet<?> onChannelReadModify(Packet<?> packet) {
        if (Quark.getInstance() == null) return packet;
        EventPacketReceive event = new EventPacketReceive(packet);
        Quark.getInstance().getEventBus().post(event);
        return event.getPacket() != null ? event.getPacket() : packet;
    }
}
