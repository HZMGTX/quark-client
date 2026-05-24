package com.ghostclient.mixin;

import com.ghostclient.GhostClient;
import com.ghostclient.event.events.EventAttack;
import com.ghostclient.module.modules.combat.Reach;
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
        if (GhostClient.getInstance() == null) return;
        EventAttack event = new EventAttack(target);
        GhostClient.getInstance().getEventBus().post(event);
        if (event.isCancelled()) ci.cancel();
    }

    @ModifyReturnValue(method = "getReachDistance", at = @At("RETURN"))
    private float modifyReach(float original) {
        if (GhostClient.getInstance() == null) return original;
        Reach reach = GhostClient.getInstance().getModuleManager().getModule(Reach.class);
        if (reach != null && reach.isEnabled()) {
            return (float) reach.getCurrentReach();
        }
        return original;
    }
}
