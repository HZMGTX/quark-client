package com.ghostclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * General mathematical utilities used across GhostClient modules.
 */
public final class MathUtil {

    private MathUtil() {}

    private static final MinecraftClient MC = MinecraftClient.getInstance();

    // -------------------------------------------------------------------------
    // Rotations
    // -------------------------------------------------------------------------

    /**
     * Compute the yaw and pitch needed to look at the given world-space position
     * from the local player's eye position.
     *
     * @param x target X
     * @param y target Y
     * @param z target Z
     * @return float[2] where [0] = yaw (-180..180) and [1] = pitch (-90..90)
     */
    public static float[] getRotations(double x, double y, double z) {
        if (MC.player == null) return new float[]{0f, 0f};

        Vec3d eyes = MC.player.getEyePos();
        double dx = x - eyes.x;
        double dy = y - eyes.y;
        double dz = z - eyes.z;

        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        return new float[]{yaw, pitch};
    }

    /**
     * Compute rotations toward an entity's eye position.
     *
     * @param entity target entity
     * @return float[2] {yaw, pitch}
     */
    public static float[] getRotationsToEntity(Entity entity) {
        Vec3d pos = entity.getEyePos();
        return getRotations(pos.x, pos.y, pos.z);
    }

    // -------------------------------------------------------------------------
    // Distance
    // -------------------------------------------------------------------------

    /**
     * Distance from the local player to the given entity (eye-to-eye).
     *
     * @param entity target
     * @return distance in blocks, or {@link Double#MAX_VALUE} if player is null
     */
    public static double getDistanceTo(Entity entity) {
        if (MC.player == null) return Double.MAX_VALUE;
        return MC.player.getEyePos().distanceTo(entity.getEyePos());
    }

    // -------------------------------------------------------------------------
    // Angle utilities
    // -------------------------------------------------------------------------

    /**
     * Wrap an angle into the range [-180, 180].
     *
     * @param angle angle in degrees
     * @return wrapped angle
     */
    public static float wrapAngle(float angle) {
        return MathHelper.wrapDegrees(angle);
    }

    /**
     * Linearly interpolate between {@code a} and {@code b} by factor {@code t} (0–1).
     *
     * @param a start value
     * @param b end value
     * @param t interpolation factor
     * @return interpolated value
     */
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    // -------------------------------------------------------------------------
    // FOV check
    // -------------------------------------------------------------------------

    /**
     * Returns whether the given entity is within the player's field of view.
     *
     * @param entity entity to check
     * @param fov    half-angle of the FOV cone in degrees (e.g. 90 = full 180° cone)
     * @return true if the entity is within the FOV
     */
    public static boolean isInFOV(Entity entity, float fov) {
        if (MC.player == null) return false;

        Vec3d eyes = MC.player.getEyePos();
        Vec3d target = entity.getEyePos();

        float[] rotations = getRotations(target.x, target.y, target.z);
        float yawDiff = Math.abs(wrapAngle(rotations[0] - MC.player.getYaw()));

        return yawDiff <= fov;
    }

    // -------------------------------------------------------------------------
    // Miscellaneous
    // -------------------------------------------------------------------------

    /**
     * Clamp a double value between min and max.
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamp a float value between min and max.
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Round a double to {@code decimals} decimal places.
     */
    public static double round(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }

    /**
     * Calculate the horizontal speed of the local player in blocks/tick.
     */
    public static double getHorizontalSpeed() {
        if (MC.player == null) return 0.0;
        Vec3d vel = MC.player.getVelocity();
        return Math.sqrt(vel.x * vel.x + vel.z * vel.z);
    }
}
