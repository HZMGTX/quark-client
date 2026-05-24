package cc.quark.mixin;

import cc.quark.Quark;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {

    /**
     * Expose stepHeight so the Step module can write to it directly via the
     * public accessor added in Step.java (which casts the player to this type).
     */
    @Shadow
    public float stepHeight;

    /**
     * Hook setVelocity(Vec3d) â€“ used by the Velocity module to cancel or
     * dampen knock-back applied by the server (EntityVelocityUpdateS2CPacket
     * eventually calls this method on the client entity).
     *
     * We do NOT fire a cancellable event here to keep it lightweight; the
     * Velocity module subscribes to EventPacketReceive and modifies the packet
     * before it reaches this call.  The hook is kept as an extension point.
     */
    @Inject(method = "setVelocity(Lnet/minecraft/util/math/Vec3d;)V",
            at = @At("HEAD"),
            cancellable = false)
    private void onSetVelocity(Vec3d velocity, CallbackInfo ci) {
        // Extension point: future modules may post an event here.
    }
}
