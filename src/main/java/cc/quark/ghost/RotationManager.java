package cc.quark.ghost;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class RotationManager {

    public static final RotationManager INSTANCE = new RotationManager();

    // -------------------------------------------------------------------------
    // Smoothing mode enum
    // -------------------------------------------------------------------------

    /**
     * Controls how the rotation manager interpolates from the current server
     * rotation toward the requested target rotation each tick.
     */
    public enum SmoothMode {
        /** Snap immediately to the target – original behaviour. */
        INSTANT,
        /** Move at most {@code getRotationSpeed()} degrees per tick (linear). */
        LINEAR,
        /** Exponential lerp using {@link #smoothFactor} (0 < factor ≤ 1). */
        EXPONENTIAL,
        /** Bezier/humanized curve produced by {@link HumanizationEngine#smoothRotate}
         *  with a small Gaussian noise added on top. */
        BEZIER
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private float serverYaw, serverPitch;
    private float targetYaw, targetPitch;
    private float visualYaw, visualPitch;
    private boolean rotating = false;
    private boolean silent = false;
    private int priority = 0;

    /** Active smoothing mode applied during {@link #onTick()}. */
    private SmoothMode smoothMode = SmoothMode.EXPONENTIAL;

    /**
     * Interpolation factor used by {@link SmoothMode#EXPONENTIAL} and
     * {@link SmoothMode#BEZIER} (0 exclusive … 1 inclusive).
     * Typical human-like range: 0.15 – 0.35.
     */
    private float smoothFactor = 0.3f;

    private final HumanizationEngine humanizer = new HumanizationEngine();

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    private RotationManager() {}

    // -------------------------------------------------------------------------
    // Rotation requests
    // -------------------------------------------------------------------------

    /**
     * Requests a rotation with the default smooth mode and factor.
     */
    public void requestRotation(float yaw, float pitch, int prio, boolean isSilent) {
        requestRotation(yaw, pitch, prio, isSilent, smoothMode, smoothFactor);
    }

    /**
     * Requests a rotation with an explicit smooth mode and factor.
     *
     * @param yaw      desired server yaw   (degrees)
     * @param pitch    desired server pitch (degrees)
     * @param prio     priority – higher values override lower ones within a tick
     * @param isSilent {@code true} = server-side only, player's visual head stays
     * @param mode     smoothing algorithm to use for this request
     * @param factor   interpolation factor (used by EXPONENTIAL and BEZIER)
     */
    public void requestRotation(float yaw, float pitch, int prio, boolean isSilent,
                                SmoothMode mode, float factor) {
        if (prio >= this.priority) {
            this.targetYaw   = yaw;
            this.targetPitch = pitch;
            this.priority    = prio;
            this.rotating    = true;
            this.silent      = isSilent;
            this.smoothMode  = mode;
            this.smoothFactor = MathHelper.clamp(factor, 0.001f, 1.0f);
        }
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            rotating  = false;
            priority  = 0;
            return;
        }

        ClientPlayerEntity player = mc.player;

        if (rotating) {
            float fromYaw   = serverYaw;
            float fromPitch = serverPitch;

            float[] next = switch (smoothMode) {
                case INSTANT     -> new float[]{ targetYaw, targetPitch };
                case LINEAR      -> computeLinear(fromYaw, fromPitch);
                case EXPONENTIAL -> humanizer.smoothRotate(fromYaw, fromPitch,
                                        targetYaw, targetPitch, smoothFactor);
                case BEZIER      -> computeBezier(fromYaw, fromPitch);
            };

            serverYaw   = next[0];
            serverPitch = MathHelper.clamp(next[1], -90f, 90f);

            if (!silent) {
                player.setYaw(serverYaw);
                player.setPitch(serverPitch);
                player.headYaw = serverYaw;
            }
        } else {
            serverYaw   = player.getYaw();
            serverPitch = player.getPitch();
        }

        visualYaw   = player.getYaw();
        visualPitch = player.getPitch();

        rotating = false;
        priority = 0;
    }

    // -------------------------------------------------------------------------
    // Smoothing helpers
    // -------------------------------------------------------------------------

    /**
     * Linear step: move at most {@code rotationSpeed} degrees per tick toward target.
     */
    private float[] computeLinear(float fromYaw, float fromPitch) {
        double speed = GhostManager.INSTANCE.getRotationSpeed();
        float maxStep = (float) speed;

        float yawDiff   = wrapAngle(targetYaw - fromYaw);
        float pitchDiff = targetPitch - fromPitch;

        float newYaw   = fromYaw   + MathHelper.clamp(yawDiff,   -maxStep, maxStep);
        float newPitch = fromPitch + MathHelper.clamp(pitchDiff, -maxStep, maxStep);
        return new float[]{ newYaw, newPitch };
    }

    /**
     * Bezier / humanized smooth rotate followed by a small Gaussian noise jitter.
     */
    private float[] computeBezier(float fromYaw, float fromPitch) {
        float[] smoothed = humanizer.smoothRotate(fromYaw, fromPitch,
                targetYaw, targetPitch, smoothFactor);
        // Add subtle Gaussian noise to make the curve look human
        float[] humanized = humanizer.humanizeRotation(smoothed[0], smoothed[1], 0.5f);
        return humanized;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private float[] interpolateRotation(float fromYaw, float fromPitch,
                                        float toYaw, float toPitch, float factor) {
        float yawDiff   = wrapAngle(toYaw - fromYaw);
        float pitchDiff = toPitch - fromPitch;
        return new float[]{
            fromYaw   + yawDiff   * factor,
            fromPitch + pitchDiff * factor
        };
    }

    private float wrapAngle(float angle) {
        angle %= 360f;
        if (angle >= 180f)  angle -= 360f;
        if (angle < -180f) angle += 360f;
        return angle;
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public float getServerYaw()   { return serverYaw; }
    public float getServerPitch() { return serverPitch; }
    public float getVisualYaw()   { return visualYaw; }
    public float getVisualPitch() { return visualPitch; }
    public boolean isRotating()   { return rotating; }

    public void setSilent(boolean silent) { this.silent = silent; }
    public boolean isSilent() { return silent; }

    public SmoothMode getSmoothMode() { return smoothMode; }
    public void setSmoothMode(SmoothMode mode) { this.smoothMode = mode; }

    public float getSmoothFactor() { return smoothFactor; }
    public void setSmoothFactor(float factor) {
        this.smoothFactor = MathHelper.clamp(factor, 0.001f, 1.0f);
    }

    public HumanizationEngine getHumanizer() { return humanizer; }
}
