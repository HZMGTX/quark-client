package com.ghostclient.mixin;

import com.ghostclient.GhostClient;
import com.ghostclient.module.modules.movement.ElytraFly;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinAbstractClientPlayerEntity {

    @Inject(method = "isUsingElytra", at = @At("RETURN"), cancellable = true)
    private void onIsUsingElytra(CallbackInfoReturnable<Boolean> cir) {
        if (GhostClient.getInstance() == null) return;
        ElytraFly elytraFly = GhostClient.getInstance().getModuleManager().getModule(ElytraFly.class);
        if (elytraFly != null && elytraFly.isEnabled() && elytraFly.isFakeElytra()) {
            cir.setReturnValue(true);
        }
    }
}
