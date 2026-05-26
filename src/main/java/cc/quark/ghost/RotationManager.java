package cc.quark.ghost;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayDeque;
import java.util.Deque;

public class RotationManager {

    public static final RotationManager INSTANCE = new RotationManager();

    public enum SmoothMode {
        INSTANT,
        LINEAR,
        EXPONENTIAL,
        BEZIER,
        HUMAN
    }

    private float serverYaw, serverPitch;
    private float realYaw, realPitch;
    private float targetYaw, targetPitch;
    private float visualYaw, visualPitch;
    private boolean rotating  = false;
    private boolean silent    = false;
    private boolean setBack   = true;
    private int priority      = 0;
    private boolean attacked  = false;

    private boolean gcdFix    = false;
    private float   lastSentYaw   = 0f;
    private float   lastSentPitch = 0f;
    private static final float GCD_BASE = 0.15f;

    private SmoothMode smoothMode  = SmoothMode.EXPONENTIAL;
    private float smoothFactor     = 0.3f;

    private final Deque<float[]> rotationHistory = new ArrayDeque<>();
    private static final int HISTORY_LIMIT = 5;

    private final HumanizationEngine humanizer = new HumanizationEngine();

    private RotationManager() {}

    public void requestRotation(float yaw, float pitch, int prio, boolean isSilent) {
        requestRotation(yaw, pitch, prio, isSilent, smoothMode, smoothFactor);
    }

    public void requestRotation(float yaw, float pitch, float maxTurn, boolean isSilent) {
        if (prio_isDefault()) {
            requestRotation(yaw, pitch, 0, isSilent, smoothMode, smoothFactor);
        } else {
            requestRotation(yaw, pitch, 0, isSilent, smoothMode, smoothFactor);
        }
    }

    private boolean prio_isDefault() { return priority == 0; }

    public void requestRotation(float yaw, float pitch, int prio, boolean isSilent,
                                SmoothMode mode, float factor) {
        if (prio >= this.priority) {
            this.targetYaw    = yaw;
            this.targetPitch  = pitch;
            this.priority     = prio;
            this.rotating     = true;
            this.silent       = isSilent;
            this.smoothMode   = mode;
            this.smoothFactor = MathHelper.clamp(factor, 0.001f, 1.0f);
        }
    }

    public void notifyAttacked() {
        this.attacked = true;
    }

    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            rotating = false;
            priority = 0;
            attacked = false;
            return;
        }

        ClientPlayerEntity player = mc.player;

        if (setBack && silent) {
            realYaw   = player.getYaw();
            realPitch = player.getPitch();
        }

        if (rotating) {
            float fromYaw   = serverYaw;
            float fromPitch = serverPitch;

            float[] next = switch (smoothMode) {
                case INSTANT     -> new float[]{ targetYaw, targetPitch };
                case LINEAR      -> computeLinear(fromYaw, fromPitch);
                case EXPONENTIAL -> humanizer.smoothRotate(fromYaw, fromPitch,
                                        targetYaw, targetPitch, smoothFactor);
                case BEZIER      -> computeBezier(fromYaw, fromPitch);
                case HUMAN       -> computeHuman(fromYaw, fromPitch);
            };

            if (gcdFix) {
                next = applyGCD(next[0], next[1]);
            }

            serverYaw   = next[0];
            serverPitch = MathHelper.clamp(next[1], -90f, 90f);

            pushHistory(serverYaw, serverPitch);

            if (!silent) {
                player.setYaw(serverYaw);
                player.setPitch(serverPitch);
                player.headYaw = serverYaw;
            }
        } else {
            serverYaw   = player.getYaw();
            serverPitch = player.getPitch();
        }

        if (setBack && silent && attacked) {
            player.setYaw(realYaw);
            player.setPitch(realPitch);
            player.headYaw = realYaw;
            attacked = false;
        }

        visualYaw   = player.getYaw();
        visualPitch = player.getPitch();

        rotating = false;
        priority = 0;
    }

    private float[] computeLinear(float fromYaw, float fromPitch) {
        double speed  = GhostManager.INSTANCE.getRotationSpeed();
        float maxStep = (float) speed;

        float yawDiff   = wrapAngle(targetYaw - fromYaw);
        float pitchDiff = targetPitch - fromPitch;

        float newYaw   = fromYaw   + MathHelper.clamp(yawDiff,   -maxStep, maxStep);
        float newPitch = fromPitch + MathHelper.clamp(pitchDiff, -maxStep, maxStep);
        return new float[]{ newYaw, newPitch };
    }

    private float[] computeBezier(float fromYaw, float fromPitch) {
        float[] smoothed  = humanizer.smoothRotate(fromYaw, fromPitch,
                targetYaw, targetPitch, smoothFactor);
        return humanizer.applyMicroJitter(smoothed[0], smoothed[1]);
    }

    private float[] computeHuman(float fromYaw, float fromPitch) {
        double difficulty = 1.0 - (double) smoothFactor;
        long reactionMs = humanizer.getReactionDelayMs(difficulty);

        float progress = MathHelper.clamp(smoothFactor + (float)(1.0 / (reactionMs / 50.0 + 1.0)), 0.001f, 1.0f);

        float yawDist   = Math.abs(MathHelper.wrapDegrees(targetYaw - fromYaw));
        float pitchDist = Math.abs(targetPitch - fromPitch);
        float dist      = (float) Math.sqrt(yawDist * yawDist + pitchDist * pitchDist);

        int bezierSteps = Math.max(1, (int)(dist / Math.max(1.0f, GhostManager.INSTANCE.getRotationSpeed())));
        float[][] path  = humanizer.generateBezierPath(fromYaw, fromPitch, targetYaw, targetPitch, bezierSteps);

        float[] nextPos = (path.length > 0) ? path[0] : new float[]{ targetYaw, targetPitch };

        return humanizer.applyMicroJitter(nextPos[0], nextPos[1]);
    }

    private float[] applyGCD(float yaw, float pitch) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options == null) return new float[]{ yaw, pitch };

        float sensitivity = (float) mc.options.getMouseSensitivity().getValue();
        float gcd = computeGCD(sensitivity);

        float dYaw   = yaw   - lastSentYaw;
        float dPitch = pitch - lastSentPitch;

        if (gcd > 0.0f) {
            dYaw   = Math.round(dYaw   / gcd) * gcd;
            dPitch = Math.round(dPitch / gcd) * gcd;
        }

        float newYaw   = lastSentYaw   + dYaw;
        float newPitch = lastSentPitch + dPitch;

        lastSentYaw   = newYaw;
        lastSentPitch = newPitch;

        return new float[]{ newYaw, MathHelper.clamp(newPitch, -90f, 90f) };
    }

    private float computeGCD(float sensitivity) {
        double f = sensitivity * 0.6 + 0.2;
        return (float)(f * f * f * 8.0 * 0.15);
    }

    private void pushHistory(float yaw, float pitch) {
        if (rotationHistory.size() >= HISTORY_LIMIT) {
            rotationHistory.pollFirst();
        }
        rotationHistory.addLast(new float[]{ yaw, pitch });
    }

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

    public float getServerYaw()   { return serverYaw; }
    public float getServerPitch() { return serverPitch; }
    public float getVisualYaw()   { return visualYaw; }
    public float getVisualPitch() { return visualPitch; }
    public boolean isRotating()   { return rotating; }

    public void setSilent(boolean silent)   { this.silent = silent; }
    public boolean isSilent()              { return silent; }

    public void setSetBack(boolean setBack) { this.setBack = setBack; }
    public boolean isSetBack()             { return setBack; }

    public void setGcdFix(boolean gcdFix)  { this.gcdFix = gcdFix; }
    public boolean isGcdFix()             { return gcdFix; }

    public SmoothMode getSmoothMode()      { return smoothMode; }
    public void setSmoothMode(SmoothMode mode) { this.smoothMode = mode; }

    public float getSmoothFactor()         { return smoothFactor; }
    public void setSmoothFactor(float factor) {
        this.smoothFactor = MathHelper.clamp(factor, 0.001f, 1.0f);
    }

    public Deque<float[]> getRotationHistory() { return rotationHistory; }

    public HumanizationEngine getHumanizer() { return humanizer; }
}
