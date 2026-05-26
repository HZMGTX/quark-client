package cc.quark.ghost;

import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class HumanizationEngine {

    private final Random random;
    private final long sessionStart;

    private static final double LOG_NORMAL_MEAN = 500.0;
    private static final double LOG_NORMAL_SIGMA = 0.3;

    public HumanizationEngine() {
        this.random = new Random(System.currentTimeMillis());
        this.sessionStart = System.currentTimeMillis();
    }

    public HumanizationEngine(long seed) {
        this.random = new Random(seed);
        this.sessionStart = System.currentTimeMillis();
    }

    public double nextGaussian(double mean, double stddev) {
        double u1 = random.nextDouble();
        double u2 = random.nextDouble();
        if (u1 == 0.0) u1 = Double.MIN_VALUE;
        double z = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
        return mean + z * stddev;
    }

    public long generateLogNormalClickInterval() {
        double mu = Math.log(LOG_NORMAL_MEAN) - 0.5 * LOG_NORMAL_SIGMA * LOG_NORMAL_SIGMA;
        double z = nextGaussian(0.0, 1.0);
        double intervalMs = Math.exp(mu + LOG_NORMAL_SIGMA * z);
        intervalMs = Math.max(50.0, Math.min(2000.0, intervalMs));
        return (long) intervalMs;
    }

    public long generateLogNormalClickInterval(double meanMs, double sigma) {
        double mu = Math.log(meanMs) - 0.5 * sigma * sigma;
        double z = nextGaussian(0.0, 1.0);
        double intervalMs = Math.exp(mu + sigma * z);
        intervalMs = Math.max(30.0, Math.min(3000.0, intervalMs));
        return (long) intervalMs;
    }

    public float[] humanizeRotation(float yaw, float pitch, float maxDeviation) {
        float jitterYaw   = (float) nextGaussian(0.0, maxDeviation);
        float jitterPitch = (float) nextGaussian(0.0, maxDeviation * 0.5);
        return new float[]{ yaw + jitterYaw, pitch + jitterPitch };
    }

    public float[] applyMicroJitter(float yaw, float pitch) {
        float jitterYaw   = (float) nextGaussian(0.0, 0.3);
        float jitterPitch = (float) nextGaussian(0.0, 0.3);
        return new float[]{ yaw + jitterYaw, MathHelper.clamp(pitch + jitterPitch, -90f, 90f) };
    }

    public double[] generateMovementNoise() {
        double dx = nextGaussian(0.0, 0.002);
        double dz = nextGaussian(0.0, 0.002);
        dx = MathHelper.clamp((float) dx, -0.005f, 0.005f);
        dz = MathHelper.clamp((float) dz, -0.005f, 0.005f);
        return new double[]{ dx, dz };
    }

    public float[] smoothRotate(float currentYaw, float currentPitch,
                                float targetYaw, float targetPitch,
                                float factor) {
        factor = Math.max(0.001f, Math.min(1.0f, factor));

        float yawDiff = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float newYaw  = currentYaw + yawDiff * factor;

        float pitchDiff = targetPitch - currentPitch;
        float newPitch  = currentPitch + pitchDiff * factor;
        newPitch = MathHelper.clamp(newPitch, -90f, 90f);

        return new float[]{ newYaw, newPitch };
    }

    public float[][] generateBezierPath(float fromYaw, float fromPitch,
                                         float toYaw, float toPitch,
                                         int steps) {
        if (steps <= 0) return new float[][]{{ toYaw, toPitch }};

        float midYaw   = (fromYaw + toYaw) / 2.0f;
        float midPitch = (fromPitch + toPitch) / 2.0f;

        float yawDist   = MathHelper.wrapDegrees(toYaw - fromYaw);
        float pitchDist = toPitch - fromPitch;
        float dist = (float) Math.sqrt(yawDist * yawDist + pitchDist * pitchDist);

        float overshootScale = dist * 0.15f;
        float perpYaw   = -pitchDist / (dist + 0.001f);
        float perpPitch = yawDist  / (dist + 0.001f);

        float ctrlYaw   = midYaw   + perpYaw   * overshootScale * (float) nextGaussian(1.0, 0.2);
        float ctrlPitch = midPitch + perpPitch * overshootScale * 0.5f * (float) nextGaussian(1.0, 0.2);

        float[][] path = new float[steps][2];
        for (int i = 0; i < steps; i++) {
            double t = (double)(i + 1) / steps;
            double oneMinusT = 1.0 - t;
            double bYaw   = oneMinusT * oneMinusT * fromYaw   +
                            2.0 * oneMinusT * t * ctrlYaw     +
                            t * t * toYaw;
            double bPitch = oneMinusT * oneMinusT * fromPitch +
                            2.0 * oneMinusT * t * ctrlPitch   +
                            t * t * toPitch;
            path[i][0] = (float) bYaw;
            path[i][1] = MathHelper.clamp((float) bPitch, -90f, 90f);
        }
        return path;
    }

    public float[] getMouseMovementStep(float fromYaw, float fromPitch,
                                         float toYaw, float toPitch,
                                         int step, int totalSteps) {
        if (totalSteps <= 0) return new float[]{ toYaw, toPitch };

        double t = (double) step / totalSteps;

        double midYaw   = (fromYaw + toYaw) / 2.0;
        double midPitch = (fromPitch + toPitch) / 2.0;

        double dist = Math.sqrt(
                Math.pow(MathHelper.wrapDegrees(toYaw - fromYaw), 2) +
                Math.pow(toPitch - fromPitch, 2));

        double devScale = dist * 0.15;
        double ctrlYaw   = midYaw   + Math.sin(step * 1.3) * devScale;
        double ctrlPitch = midPitch + Math.cos(step * 1.3) * devScale * 0.5;

        double oneMinusT = 1.0 - t;
        double bYaw   = oneMinusT * oneMinusT * fromYaw   +
                        2.0 * oneMinusT * t * ctrlYaw     +
                        t * t * toYaw;
        double bPitch = oneMinusT * oneMinusT * fromPitch +
                        2.0 * oneMinusT * t * ctrlPitch   +
                        t * t * toPitch;

        return new float[]{ (float) bYaw, (float) MathHelper.clamp((float) bPitch, -90f, 90f) };
    }

    public long getReactionDelayMs(double difficulty) {
        difficulty = Math.max(0.0, Math.min(1.0, difficulty));
        double minReaction = 150.0;
        double maxReaction = 350.0;
        double mean = minReaction + (maxReaction - minReaction) * difficulty;
        double stddev = 30.0 + difficulty * 20.0;
        long delay = (long) nextGaussian(mean, stddev);
        return Math.max((long) minReaction, Math.min((long) maxReaction, delay));
    }

    public double getFatigueMultiplier(long sessionMs) {
        double sessionMinutes = sessionMs / 60000.0;
        double fatigue = 1.0 + Math.min(0.4, sessionMinutes / 120.0 * 0.4);
        double noise = nextGaussian(0.0, 0.02);
        return Math.max(1.0, Math.min(1.4, fatigue + noise));
    }

    public double getFatigueMultiplier() {
        long sessionMs = System.currentTimeMillis() - sessionStart;
        return getFatigueMultiplier(sessionMs);
    }

    public boolean shouldClick(int minCPS, int maxCPS, long lastClick) {
        double mean = (minCPS + maxCPS) / 2.0;
        double sigma = (maxCPS - minCPS) / 4.0;
        double cps = nextGaussian(mean, sigma);
        cps = Math.max(minCPS, Math.min(maxCPS, cps));

        long intervalMs = (long) (1000.0 / cps);
        return System.currentTimeMillis() - lastClick >= intervalMs;
    }

    public long randomDelay(long base, long variance) {
        long result = base + (long) nextGaussian(0.0, variance);
        return Math.max(0L, result);
    }

    public boolean isTooFast(long lastAction, long minInterval) {
        return System.currentTimeMillis() - lastAction < minInterval;
    }

    public long getSessionDurationMs() {
        return System.currentTimeMillis() - sessionStart;
    }

    public Random getRandom() {
        return random;
    }
}
