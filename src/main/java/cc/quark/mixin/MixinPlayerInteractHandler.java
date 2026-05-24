package cc.quark.mixin;

import cc.quark.Quark;
import cc.quark.event.events.EventAttack;
import cc.quark.module.modules.combat.Reach;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyReturnValue;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinPlayerInteractHandler {

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (Quark.getInstance() == null) return;
        EventAttack event = new EventAttack(target);
        Quark.getInstance().getEventBus().post(event);
        if (event.isCancelled()) ci.cancel();
    }

    @ModifyReturnValue(method = "getReachDistance", at = @At("RETURN"))
    private float modifyReach(float original) {
        if (Quark.getInstance() == null) return original;
        Reach reach = Quark.getInstance().getModuleManager().getModule(Reach.class);
        if (reach != null && reach.isEnabled()) {
            return (float) reach.getCurrentReach();
        }
        return original;
    }
}
