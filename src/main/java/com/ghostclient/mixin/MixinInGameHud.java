package com.ghostclient.mixin;

import com.ghostclient.GhostClient;
import com.ghostclient.event.events.EventRender2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderReturn(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (GhostClient.getInstance() == null) return;
        GhostClient.getInstance().getEventBus().post(new EventRender2D(context, tickDelta));
    }
}
