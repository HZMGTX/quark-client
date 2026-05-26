package cc.quark.mixin;

import cc.quark.Quark;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventBlockBreak;
import cc.quark.module.modules.combat.Reach;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinPlayerInteractHandler {

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (Quark.getInstance() == null) return;
        EventAttack event = new EventAttack(target);
        Quark.getInstance().getEventBus().post(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "breakBlock", at = @At("HEAD"))
    private void onBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (Quark.getInstance() == null || MinecraftClient.getInstance().world == null) return;
        BlockState state = MinecraftClient.getInstance().world.getBlockState(pos);
        Quark.getInstance().getEventBus().post(new EventBlockBreak(pos, state));
    }

    // Reach is an attribute in 1.21.1
}
