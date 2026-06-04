package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.Vec3d;

public class FastSwim2 extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Swimming speed multiplier", 0.4, 0.1, 2.0));

    public FastSwim2() {
        super("FastSwim2", "Ultra-fast swimming with dolphin's grace", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isTouchingWater()) return;

        // Apply dolphin's grace effect client-side for visual
        if (!mc.player.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.DOLPHINS_GRACE, 40, 0, false, false));
        }

        // Boost swimming velocity
        float yaw = (float) Math.toRadians(mc.player.getYaw());
        float pitch = (float) Math.toRadians(mc.player.getPitch());
        double s = speed.get();

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(
                -Math.sin(yaw) * Math.cos(pitch) * s,
                vel.y,
                Math.cos(yaw) * Math.cos(pitch) * s
        );
    }
}
