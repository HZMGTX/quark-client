package cc.quark.mixin;

import cc.quark.Quark;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.ModuleManager;
import cc.quark.module.modules.render.Zoom;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyReturnValue;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "renderWorld", at = @At(value = "TAIL"))
    private void onRenderWorldTail(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        if (Quark.getInstance() == null) return;
        Quark.getInstance().getEventBus().post(new EventRender3D(matrices, tickDelta));
    }

    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private double modifyFov(double original) {
        if (Quark.getInstance() == null) return original;
        ModuleManager mm = Quark.getInstance().getModuleManager();
        if (mm == null) return original;
        Zoom zoom = mm.getModule(Zoom.class);
        if (zoom != null && zoom.isEnabled()) {
            return zoom.getModifiedFov(original);
        }
        return original;
    }
}
