package com.ghostclient.ghost;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class RotationManager {

    public static final RotationManager INSTANCE = new RotationManager();

    private float serverYaw, serverPitch;
    private float targetYaw, targetPitch;
    private float visualYaw, visualPitch;
    private boolean rotating = false;
    private boolean silent = false;
    private int priority = 0;

    private RotationManager() {}

    public void requestRotation(float yaw, float pitch, int prio, boolean isSilent) {
        if (prio >= this.priority) {
            this.targetYaw = yaw;
            this.targetPitch = pitch;
            this.priority = prio;
            this.rotating = true;
            this.silent = isSilent;
        }
    }

    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            rotating = false;
            priority = 0;
            return;
        }

        ClientPlayerEntity player = mc.player;

        if (rotating) {
            serverYaw = targetYaw;
            serverPitch = targetPitch;

            if (!silent) {
                player.setYaw(serverYaw);
                player.setPitch(serverPitch);
                player.headYaw = serverYaw;
            }
        } else {
            serverYaw = player.getYaw();
            serverPitch = player.getPitch();
        }

        visualYaw = player.getYaw();
        visualPitch = player.getPitch();

        rotating = false;
        priority = 0;
    }

    private float[] interpolateRotation(float fromYaw, float fromPitch, float toYaw, float toPitch, float factor) {
        float yawDiff = wrapAngle(toYaw - fromYaw);
        float pitchDiff = toPitch - fromPitch;
        return new float[]{
            fromYaw + yawDiff * factor,
            fromPitch + pitchDiff * factor
        };
    }

    private float wrapAngle(float angle) {
        angle %= 360f;
        if (angle >= 180f) angle -= 360f;
        if (angle < -180f) angle += 360f;
        return angle;
    }

    public float getServerYaw()   { return serverYaw; }
    public float getServerPitch() { return serverPitch; }
    public float getVisualYaw()   { return visualYaw; }
    public float getVisualPitch() { return visualPitch; }
    public boolean isRotating()   { return rotating; }

    public void setSilent(boolean silent) { this.silent = silent; }
    public boolean isSilent() { return silent; }
}
