package cc.quark.mixin;

import cc.quark.Quark;
import cc.quark.command.CommandManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
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
        }
    }
}
