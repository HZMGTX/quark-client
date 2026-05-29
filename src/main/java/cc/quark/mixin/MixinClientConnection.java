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
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    // ThreadLocals pass the already-posted event to the cancel @Inject so the
    // event bus is only called once per packet rather than twice.
    private static final ThreadLocal<EventPacketSend>    PENDING_SEND    = new ThreadLocal<>();
    private static final ThreadLocal<EventPacketReceive> PENDING_RECEIVE = new ThreadLocal<>();

    // ---- Outgoing packets ------------------------------------------------

    // Step 1: post the event and allow packet replacement via the return value.
    // @ModifyArg fires before @Inject at the same injection point.
    @ModifyArg(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V",
               at = @At("HEAD"), index = 0)
    private Packet<?> onSendModifyArg(Packet<?> packet) {
        if (Quark.getInstance() == null) return packet;
        EventPacketSend event = new EventPacketSend(packet);
        Quark.getInstance().getEventBus().post(event);
        PENDING_SEND.set(event);
        Packet<?> replaced = event.getPacket();
        return (replaced != null) ? replaced : packet;
    }

    // Step 2: cancel the method call if the event was cancelled.
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V",
            at = @At("HEAD"), cancellable = true)
    private void onSendInject(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        EventPacketSend event = PENDING_SEND.get();
        PENDING_SEND.remove();
        if (event != null && event.isCancelled()) {
            ci.cancel();
        }
    }

    // ---- Incoming packets ------------------------------------------------

    @ModifyArg(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V",
               at = @At("HEAD"), index = 1)
    private Packet<?> onReceiveModifyArg(Packet<?> packet) {
        if (Quark.getInstance() == null) return packet;
        EventPacketReceive event = new EventPacketReceive(packet);
        Quark.getInstance().getEventBus().post(event);
        PENDING_RECEIVE.set(event);
        Packet<?> replaced = event.getPacket();
        return (replaced != null) ? replaced : packet;
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V",
            at = @At("HEAD"), cancellable = true)
    private void onReceiveInject(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        EventPacketReceive event = PENDING_RECEIVE.get();
        PENDING_RECEIVE.remove();
        if (event != null && event.isCancelled()) {
            ci.cancel();
        }
    }
}
