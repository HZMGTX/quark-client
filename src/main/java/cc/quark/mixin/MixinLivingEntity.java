package cc.quark.mixin;

import cc.quark.Quark;
import cc.quark.event.events.EventMove;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravel(Vec3d movementInput, CallbackInfo ci) {
        if (Quark.getInstance() == null) return;
        LivingEntity self = (LivingEntity)(Object)this;
        if (self != net.minecraft.client.MinecraftClient.getInstance().player) return;

        EventMove event = new EventMove(movementInput.x, movementInput.y, movementInput.z);
        Quark.getInstance().getEventBus().post(event);
    }

    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void onHandleFallDamage(float fallDistance, float damageMultiplier,
                                     DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (Quark.getInstance() == null) return;
        LivingEntity self = (LivingEntity)(Object)this;
        if (self != net.minecraft.client.MinecraftClient.getInstance().player) return;

        // NoFall module cancels fall damage via packet; this is a secondary safety net
        cc.quark.module.modules.movement.NoFall noFall =
            Quark.getInstance().getModuleManager().getModule(
                cc.quark.module.modules.movement.NoFall.class);
        if (noFall != null && noFall.isEnabled()) {
            cir.setReturnValue(false);
        }
    }
}
