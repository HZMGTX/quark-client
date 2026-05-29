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
        if (event.getYaw() != player.getYaw())     player.setYaw(event.getYaw());
        if (event.getPitch() != player.getPitch()) player.setPitch(event.getPitch());
        if (event.getX() != player.getX() || event.getY() != player.getY() || event.getZ() != player.getZ()) {
            player.setPosition(event.getX(), event.getY(), event.getZ());
        }
        if (event.isOnGround() != player.isOnGround()) {
            player.setOnGround(event.isOnGround());
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

}
