package com.ghostclient.module.modules.player;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventPacketSend;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.DoubleSetting;
import net.minecraft.client.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

/**
 * Freecam - detaches the camera from the player.
 *
 * While active the player entity is frozen in place on the server (movement
 * packets are suppressed) and a ghost camera entity is used for rendering.
 * WASD + mouse control the camera; the real player stays put.
 */
public class Freecam extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Camera movement speed", 1.0, 0.1, 10.0));

    // Saved player state
    private double savedX, savedY, savedZ;
    private float savedYaw, savedPitch;

    // Ghost camera entity (we reuse a dummy entity for the camera position)
    private FreecamEntity cameraEntity;
    private Entity originalCameraEntity;

    public Freecam() {
        super("Freecam", "Detaches camera from player, WASD moves camera", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            disable();
            return;
        }

        savedX = mc.player.getX();
        savedY = mc.player.getY();
        savedZ = mc.player.getZ();
        savedYaw = mc.player.getYaw();
        savedPitch = mc.player.getPitch();

        // Create ghost camera
        cameraEntity = new FreecamEntity(mc.world, savedX, savedY, savedZ, savedYaw, savedPitch);
        originalCameraEntity = mc.getCameraEntity();
        mc.setCameraEntity(cameraEntity);
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;

        // Restore camera to player
        if (originalCameraEntity != null) {
            mc.setCameraEntity(originalCameraEntity);
        }
        cameraEntity = null;

        // Restore player position
        mc.player.setPosition(savedX, savedY, savedZ);
        mc.player.setYaw(savedYaw);
        mc.player.setPitch(savedPitch);
        mc.player.setVelocity(Vec3d.ZERO);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || cameraEntity == null) return;

        // Move camera entity based on input
        Input input = mc.player.input;
        float yaw = cameraEntity.getYaw();
        float pitch = cameraEntity.getPitch();

        double dx = 0, dy = 0, dz = 0;
        double spd = speed.get() * 0.1;

        // Forward/backward
        if (input.pressingForward) {
            dx -= Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * spd;
            dz += Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * spd;
            dy -= Math.sin(Math.toRadians(pitch)) * spd;
        }
        if (input.pressingBack) {
            dx += Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * spd;
            dz -= Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * spd;
            dy += Math.sin(Math.toRadians(pitch)) * spd;
        }
        // Left/right strafe
        if (input.pressingLeft) {
            dx -= Math.cos(Math.toRadians(yaw)) * spd;
            dz -= Math.sin(Math.toRadians(yaw)) * spd;
        }
        if (input.pressingRight) {
            dx += Math.cos(Math.toRadians(yaw)) * spd;
            dz += Math.sin(Math.toRadians(yaw)) * spd;
        }
        // Up/down via jump/sneak
        if (input.jumping) dy += spd;
        if (input.sneaking) dy -= spd;

        cameraEntity.setPosition(
                cameraEntity.getX() + dx,
                cameraEntity.getY() + dy,
                cameraEntity.getZ() + dz
        );

        // Sync camera yaw/pitch to player's look direction
        cameraEntity.setYaw(mc.player.getYaw());
        cameraEntity.setPitch(mc.player.getPitch());

        // Freeze player movement
        mc.player.setVelocity(Vec3d.ZERO);
        mc.player.setPosition(savedX, savedY, savedZ);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        // While in freecam, suppress all movement packets to freeze player on server
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            event.cancel();
        }
    }

    // -------------------------------------------------------------------------
    // Inner class: minimal entity used as camera anchor
    // -------------------------------------------------------------------------

    public static class FreecamEntity extends net.minecraft.entity.Entity {

        public FreecamEntity(net.minecraft.world.World world, double x, double y, double z,
                             float yaw, float pitch) {
            super(net.minecraft.entity.EntityType.ITEM, world);
            setPosition(x, y, z);
            setYaw(yaw);
            setPitch(pitch);
        }

        @Override
        protected void initDataTracker() {}

        @Override
        protected void readCustomDataFromNbt(net.minecraft.nbt.NbtCompound nbt) {}

        @Override
        protected void writeCustomDataToNbt(net.minecraft.nbt.NbtCompound nbt) {}
    }
}
