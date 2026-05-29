package cc.quark.mixin;

import cc.quark.Quark;
import cc.quark.module.modules.render.NoFog;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {

    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true, require = 0)
    private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType,
                                    float viewDistance, boolean thickFog, float tickDelta,
                                    CallbackInfo ci) {
        if (Quark.getInstance() == null) return;
        NoFog module = Quark.getInstance().getModuleManager().getModule(NoFog.class);
        if (module == null || !module.isEnabled()) return;

        Entity entity = camera.getFocusedEntity();
        FluidState fluid = entity.getWorld().getFluidState(
                BlockPos.ofFloored(camera.getPos()));

        boolean inWater = fluid.isIn(FluidTags.WATER);
        boolean inLava  = fluid.isIn(FluidTags.LAVA);

        if (inWater && module.isRemovingWaterFog()) { ci.cancel(); return; }
        if (inLava  && module.isRemovingLavaFog())  { ci.cancel(); return; }
        if (!inWater && !inLava)                     { ci.cancel(); }
    }
}
