package cc.quark.mixin;

import cc.quark.Quark;
import cc.quark.command.CommandManager;
import cc.quark.event.events.EventChat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        Quark instance = Quark.getInstance();
        if (instance == null) return;

        CommandManager cm = instance.getCommandManager();
        if (cm != null && cm.onChat(message)) {
            ci.cancel();
            return;
        }

        EventChat event = new EventChat(message, false);
        instance.getEventBus().post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        Quark instance = Quark.getInstance();
        if (instance == null) return;

        String text = packet.content().getString();
        EventChat event = new EventChat(text, true);
        instance.getEventBus().post(event);
        if (event.isCancelled()) {
            ci.cancel();
            return;
        }
        String modified = event.getMessage();
        if (!modified.equals(text) && MinecraftClient.getInstance().player != null) {
            ci.cancel();
            MinecraftClient.getInstance().player.sendMessage(
                    net.minecraft.text.Text.literal(modified), false);
        }
    }
}
