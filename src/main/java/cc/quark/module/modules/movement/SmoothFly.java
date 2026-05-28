package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SmoothFly extends Module {

    private final DoubleSetting smoothness = register(new DoubleSetting("Smoothness", "Lerp ticks to reach target velocity", 8.0, 1.0, 20.0));
    private final BoolSetting autoPitch = register(new BoolSetting("Auto Pitch", "Include pitch in directional movement", true));

    private double curX = 0, curY = 0, curZ = 0;

    public SmoothFly() {
        super("SmoothFly", "Smooth elytra-like glide with lerped velocity", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        Vec3d vel = mc.player.getVelocity();
        curX = vel.x;
        curY = vel.y;
        curZ = vel.z;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        float yawRad = mc.player.getYaw() * 0.017453292f;
        float pitchRad = mc.player.getPitch() * 0.017453292f;

        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        double targetX = 0, targetY = 0, targetZ = 0;

        if (fwd != 0 || side != 0) {
            double len = Math.sqrt(fwd * fwd + side * side);
            float nFwd = (float) (fwd / len);
            float nSide = (float) (side / len);

            if (autoPitch.isEnabled()) {
                targetX = (-MathHelper.sin(yawRad) * MathHelper.cos(pitchRad) * nFwd + MathHelper.cos(yawRad) * nSide) * 0.5;
                targetY = -MathHelper.sin(pitchRad) * nFwd * 0.5;
                targetZ = (MathHelper.cos(yawRad) * MathHelper.cos(pitchRad) * nFwd + MathHelper.sin(yawRad) * nSide) * 0.5;
            } else {
                targetX = (-MathHelper.sin(yawRad) * nFwd + MathHelper.cos(yawRad) * nSide) * 0.5;
                targetZ = (MathHelper.cos(yawRad) * nFwd + MathHelper.sin(yawRad) * nSide) * 0.5;
            }
        }

        if (mc.options.jumpKey.isPressed()) targetY = 0.3;
        else if (mc.options.sneakKey.isPressed()) targetY = -0.3;
        else if (fwd == 0 && side == 0) targetY = -0.02;

        double alpha = 1.0 / smoothness.get();
        curX += (targetX - curX) * alpha;
        curY += (targetY - curY) * alpha;
        curZ += (targetZ - curZ) * alpha;

        mc.player.setVelocity(curX, curY, curZ);
        mc.player.fallDistance = 0;
    }
}
