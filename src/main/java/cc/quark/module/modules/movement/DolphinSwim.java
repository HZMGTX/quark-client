package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public class DolphinSwim extends Module {

    private final DoubleSetting strength = register(new DoubleSetting(
            "Strength", "Speed boost strength while swimming with Dolphin's Grace", 1.8, 1.0, 3.0));

    public DolphinSwim() {
        super("DolphinSwim", "Applies dolphin's grace speed boost when swimming", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc == null || mc.player == null) return;
        if (!mc.player.isTouchingWater()) return;
        if (!mc.player.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        double yawRad = Math.toRadians(mc.player.getYaw());
        double mult = strength.get();

        double dx = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side);
        double dz = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side);

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(
                vel.x + dx * (mult - 1.0) * 0.05,
                vel.y,
                vel.z + dz * (mult - 1.0) * 0.05);
    }
}
