package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public class UnderwaterSpeed extends Module {

    private final BoolSetting autoDive = register(new BoolSetting(
            "AutoDive", "Automatically look down to dive when entering water", true));

    public UnderwaterSpeed() {
        super("UnderwaterSpeed", "Increases underwater swim speed to match sprinting speed",
                Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isTouchingWater()) return;

        // Apply Dolphin's Grace for smooth server-side movement
        mc.player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.DOLPHINS_GRACE, 20, 1, false, false));

        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;
        if (!moving) return;

        mc.player.setSprinting(true);

        float yaw = mc.player.getYaw();
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        double yawRad = Math.toRadians(yaw);
        double len = Math.sqrt(fwd * fwd + side * side);
        double nFwd = fwd / len;
        double nSide = side / len;

        double sprintSpeed = 0.26;
        double dx = (-Math.sin(yawRad) * nFwd + Math.cos(yawRad) * nSide) * sprintSpeed;
        double dz = (Math.cos(yawRad) * nFwd + Math.sin(yawRad) * nSide) * sprintSpeed;

        Vec3d vel = mc.player.getVelocity();
        double dy = vel.y;

        if (autoDive.isEnabled() && mc.options.sneakKey.isPressed()) {
            dy = -0.2;
        } else if (mc.options.jumpKey.isPressed()) {
            dy = 0.2;
        }

        mc.player.setVelocity(dx, dy, dz);
    }
}
