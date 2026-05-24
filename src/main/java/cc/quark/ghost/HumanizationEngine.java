package cc.quark.ghost;

import net.minecraft.util.math.MathHelper;

import java.util.Random;

/**
 * HumanizationEngine - makes all automated actions appear human-like.
 *
 * <p>Provides Gaussian-noised rotations, Bezier-curve mouse interpolation,
 * CPS randomization, and general timing jitter to defeat pattern-detection
 * in anti-cheat systems.
 */
public class HumanizationEngine {

    private final Random random = new Random();

    // -------------------------------------------------------------------------
    // Rotation humanization
    // -------------------------------------------------------------------------

    /**
     * Adds Gaussian noise to the given yaw and pitch.
     *
     * <p>Yaw noise uses the full {@code maxDeviation}; pitch noise uses half
     * (vertical mouse movement is naturally more precise for most players).
     *
     * @param yaw          input yaw  (degrees)
     * @param pitch        input pitch (degrees)
     * @param maxDeviation maximum 1-sigma deviation in degrees
     * @return float[2] = {noisyYaw, noisyPitch}
     */
    public float[] humanizeRotation(float yaw, float pitch, float maxDeviation) {
        float jitterYaw   = (float) (random.nextGaussian() * maxDeviation);
        float jitterPitch = (float) (random.nextGaussian() * maxDeviation * 0.5f);
        return new float[]{ yaw + jitterYaw, pitch + jitterPitch };
    }

    /**
     * Smoothly interpolates {@code (currentYaw, currentPitch)} toward
     * {@code (targetYaw, targetPitch)} using exponential smoothing.
     *
     * <p>A {@code factor} of 1.0 snaps immediately; 0.0 never moves.
     * Typical human-like values: 0.15 â€“ 0.35.
     *
     * @param currentYaw   current server yaw   (degrees)
     * @param currentPitch current server pitch (degrees)
     * @param targetYaw    desired yaw  (degrees)
     * @param targetPitch  desired pitch (degrees)
     * @param factor       interpolation factor in (0, 1]
     * @return float[2] = {newYaw, newPitch}
     */
    public float[] smoothRotate(float currentYaw, float currentPitch,
                                float targetYaw, float targetPitch,
                                float factor) {
        factor = Math.max(0.001f, Math.min(1.0f, factor));

        // Handle yaw wrap-around (e.g. -179 â†’ 179 should go -2Â°, not +358Â°)
        float yawDiff = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float newYaw   = currentYaw + yawDiff * factor;

        float pitchDiff = targetPitch - currentPitch;
        float newPitch  = currentPitch + pitchDiff * factor;
        newPitch = MathHelper.clamp(newPitch, -90f, 90f);

        return new float[]{ newYaw, newPitch };
    }

    // -------------------------------------------------------------------------
    // Click timing
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} when a left-click should occur, based on a target CPS
     * range and the time of the last click.
     *
     * <p>The actual CPS is sampled from a Gaussian distribution with mean equal to
     * the midpoint of [{@code minCPS}, {@code maxCPS}] and Ïƒ = (max-min)/4, then
     * clamped to the requested range.
     *
     * @param minCPS   minimum clicks per second
     * @param maxCPS   maximum clicks per second
     * @param lastClick timestamp (ms) of the last click
     * @return true if enough time has passed for the next click
     */
    public boolean shouldClick(int minCPS, int maxCPS, long lastClick) {
        double mean = (minCPS + maxCPS) / 2.0;
        double sigma = (maxCPS - minCPS) / 4.0;
        double cps = random.nextGaussian() * sigma + mean;
        cps = Math.max(minCPS, Math.min(maxCPS, cps));

        long intervalMs = (long) (1000.0 / cps);
        return System.currentTimeMillis() - lastClick >= intervalMs;
    }

    // -------------------------------------------------------------------------
    // General timing jitter
    // -------------------------------------------------------------------------

    /**
     * Returns a delay in milliseconds drawn from a Gaussian distribution with the
     * given {@code base} (mean) and {@code variance} (standard deviation).
     *
     * <p>The result is always non-negative.
     *
     * @param base     mean delay (ms)
     * @param variance standard deviation (ms)
     * @return humanized delay (ms), always >= 0
     */
    public long randomDelay(long base, long variance) {
        long result = base + (long) (random.nextGaussian() * variance);
        return Math.max(0L, result);
    }

    // -------------------------------------------------------------------------
    // Bezier mouse interpolation
    // -------------------------------------------------------------------------

    /**
     * Computes a single step along a quadratic Bezier curve connecting
     * {@code (fromYaw, fromPitch)} to {@code (toYaw, toPitch)}.
     *
     * <p>A random control point offset is chosen once implicitly via the step
     * index, making each curve slightly different.  The control point is
     * displaced perpendicular to the straight-line path by a small random
     * amount proportional to the total angular distance.
     *
     * @param fromYaw    starting yaw   (degrees)
     * @param fromPitch  starting pitch (degrees)
     * @param toYaw      target yaw     (degrees)
     * @param toPitch    target pitch   (degrees)
     * @param step       current step index (0-based)
     * @param totalSteps total number of steps for the curve
     * @return float[2] = {yaw, pitch} at this step
     */
    public float[] getMouseMovementStep(float fromYaw, float fromPitch,
                                        float toYaw, float toPitch,
                                        int step, int totalSteps) {
        if (totalSteps <= 0) return new float[]{ toYaw, toPitch };

        double t = (double) step / totalSteps;

        // Control point: midpoint + perpendicular offset
        double midYaw   = (fromYaw + toYaw) / 2.0;
        double midPitch = (fromPitch + toPitch) / 2.0;

        // Angular distance used to scale the control-point deviation
        double dist = Math.sqrt(
                Math.pow(MathHelper.wrapDegrees(toYaw - fromYaw), 2) +
                Math.pow(toPitch - fromPitch, 2));

        // Deterministic-looking but not perfectly linear control deviation
        // We seed the random offset with step so it's consistent across a single sweep
        double devScale = dist * 0.15;
        double ctrlYaw   = midYaw   + Math.sin(step * 1.3) * devScale;
        double ctrlPitch = midPitch + Math.cos(step * 1.3) * devScale * 0.5;

        // Quadratic Bezier: B(t) = (1-t)^2 * P0 + 2*(1-t)*t * P1 + t^2 * P2
        double oneMinusT = 1.0 - t;
        double bYaw   = oneMinusT * oneMinusT * fromYaw   +
                        2.0 * oneMinusT * t * ctrlYaw     +
                        t * t * toYaw;
        double bPitch = oneMinusT * oneMinusT * fromPitch +
                        2.0 * oneMinusT * t * ctrlPitch   +
                        t * t * toPitch;

        return new float[]{ (float) bYaw, (float) MathHelper.clamp((float) bPitch, -90f, 90f) };
    }

    // -------------------------------------------------------------------------
    // Speed guard
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} when the time elapsed since {@code lastAction} is less
     * than {@code minInterval} milliseconds, indicating the action would be
     * performed too quickly for a human.
     *
     * @param lastAction  timestamp (ms) of the previous action
     * @param minInterval minimum human-realistic interval (ms)
     * @return true if acting now would look suspiciously fast
     */
    public boolean isTooFast(long lastAction, long minInterval) {
        return System.currentTimeMillis() - lastAction < minInterval;
    }
}
