package cc.quark.mixin;

import cc.quark.module.modules.render.XRay;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Inject(method = "reload", at = @At("HEAD"), require = 0)
    private void quark$onReload(CallbackInfo ci) {
        // Called when chunk data is reloaded — XRay marks pending reload cleared here
        XRay.onChunkReload();
    }
}
