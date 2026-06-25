package cc.quark.mixin;

import cc.quark.module.modules.render.XRay;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class MixinAbstractBlockState {

    @Inject(method = "isOpaque", at = @At("HEAD"), cancellable = true, require = 0)
    private void quark$xrayIsOpaque(CallbackInfoReturnable<Boolean> cir) {
        if (XRay.isXrayActive() && !XRay.isWhitelisted((BlockState)(Object)this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isSolidBlock", at = @At("HEAD"), cancellable = true, require = 0)
    private void quark$xrayIsSolidBlock(
            net.minecraft.world.BlockView world,
            net.minecraft.util.math.BlockPos pos,
            CallbackInfoReturnable<Boolean> cir) {
        if (XRay.isXrayActive() && !XRay.isWhitelisted((BlockState)(Object)this)) {
            cir.setReturnValue(false);
        }
    }
}
