package cc.quark.mixin;

import cc.quark.Quark;
import cc.quark.command.CommandManager;
import cc.quark.event.events.EventKey;
import cc.quark.event.events.EventTick;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraft {

    /**
     * Post EventTick every game tick so modules can react without subscribing
     * to Fabric's tick event directly.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Quark instance = Quark.getInstance();
        if (instance == null) return;
        instance.getEventBus().post(new EventTick());
    }

}
