package cc.quark.mixin;

import cc.quark.Quark;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.modules.render.Crosshair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter, CallbackInfo ci) {
        Crosshair ch = Crosshair.INSTANCE;
        if (ch != null && ch.isEnabled() && !ch.isShowingDefault()) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderReturn(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter, CallbackInfo ci) {
        if (Quark.getInstance() == null) return;
        Quark.getInstance().getEventBus().post(new EventRender2D(context, tickCounter.getTickDelta(true)));
    }
}
