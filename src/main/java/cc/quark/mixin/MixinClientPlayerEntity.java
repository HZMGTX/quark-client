package cc.quark.mixin;

import cc.quark.Quark;
import cc.quark.command.CommandManager;
import cc.quark.event.events.EventJump;
import cc.quark.event.events.EventPostMotion;
import cc.quark.event.events.EventPreMotion;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity {

    /**
     * Fires EventPreMotion before the client sends its position/rotation packet.
     * Modules can modify yaw, pitch, onGround via the event setters; however
     * because the packet is already formed inside sendMovementPackets() we post
     * the event here to let modules set player yaw/pitch on the player object
     * *before* the packet is built (the packet reads from the player fields).
     */
    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void onPreMotion(CallbackInfo ci) {
        Quark instance = Quark.getInstance();
        if (instance == null) return;

        ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
        EventPreMotion event = new EventPreMotion(
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYaw(),
                player.getPitch(),
                player.isOnGround()
        );
        instance.getEventBus().post(event);

        // Apply any modifications modules made to the event back onto the player
        // so the packet picks them up.
        if (event.getYaw() != player.getYaw()) {
            player.setYaw(event.getYaw());
        }
        if (event.getPitch() != player.getPitch()) {
            player.setPitch(event.getPitch());
        }
    }

    /**
     * Fires EventPostMotion after the packet has been sent.
     * Modules that spoofed rotation can restore the real values here.
     */
    @Inject(method = "sendMovementPackets", at = @At("RETURN"))
    private void onPostMotion(CallbackInfo ci) {
        Quark instance = Quark.getInstance();
        if (instance == null) return;
        instance.getEventBus().post(new EventPostMotion());
    }

    /**
     * Fires EventJump before the player actually jumps.
     * Cancelling the event prevents the jump.
     */
    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        Quark instance = Quark.getInstance();
        if (instance == null) return;
        EventJump event = new EventJump();
        instance.getEventBus().post(event);
        if (event.isCancelled()) ci.cancel();
    }

    /**
     * Intercepts outgoing chat messages to handle commands.
     * If the CommandManager consumes the message (it starts with the prefix),
     * we cancel the packet so nothing is sent to the server.
     */
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        Quark instance = Quark.getInstance();
        if (instance == null) return;
        CommandManager cm = instance.getCommandManager();
        if (cm != null && cm.onChat(message)) {
            ci.cancel();
        }
    }
}
