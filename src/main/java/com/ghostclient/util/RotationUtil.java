package com.ghostclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Utility methods for computing and smoothing yaw/pitch rotations.
 */
public final class RotationUtil {

    private RotationUtil() {}

    /**
     * Compute the yaw angle (horizontal) from the player's eye position toward a target position.
     *
     * @param target the world-space position to look at
     * @return yaw in degrees (-180..180)
     */
    public static float getYaw(Vec3d target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return 0f;
        Vec3d eyes = mc.player.getEyePos();
        double dx = target.x - eyes.x;
        double dz = target.z - eyes.z;
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    /**
     * Compute the pitch angle (vertical) from the player's eye position toward a target position.
     *
     * @param target the world-space position to look at
     * @return pitch in degrees (-90..90)
     */
    public static float getPitch(Vec3d target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return 0f;
        Vec3d eyes = mc.player.getEyePos();
        double dx = target.x - eyes.x;
        double dy = target.y - eyes.y;
        double dz = target.z - eyes.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        return (float) -Math.toDegrees(Math.atan2(dy, dist));
    }

    /**
     * Smoothly interpolate from {@code current} toward {@code target} by {@code speed} degrees per tick.
     * Handles yaw wrap-around at ±180.
     *
     * @param current current angle
     * @param target  desired angle
     * @param speed   max degrees to move per call
     * @return new angle
     */
    public static float smoothYaw(float current, float target, float speed) {
        float diff = MathHelper.wrapDegrees(target - current);
        if (Math.abs(diff) < speed) return target;
        return current + Math.signum(diff) * speed;
    }

    /**
     * Smoothly interpolate pitch toward target, clamped to ±90°.
     */
    public static float smoothPitch(float current, float target, float speed) {
        float diff = target - current;
        if (Math.abs(diff) < speed) return MathHelper.clamp(target, -90f, 90f);
        return MathHelper.clamp(current + Math.signum(diff) * speed, -90f, 90f);
    }

    /**
     * Returns the angle (degrees) between the player's current yaw and the direction
     * to the given entity center, used for FOV checks.
     */
    public static float getAngleTo(Entity entity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return 180f;
        Vec3d target = entity.getEyePos();
        float desiredYaw = getYaw(target);
        return Math.abs(MathHelper.wrapDegrees(desiredYaw - mc.player.getYaw()));
    }

    /**
     * Clamp yaw to the vanilla range [-180, 180].
     */
    public static float normalizeYaw(float yaw) {
        return MathHelper.wrapDegrees(yaw);
    }
}
