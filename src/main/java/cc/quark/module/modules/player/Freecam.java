package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.event.events.EventRender3D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
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
            "Speed", "Camera movement speed", 1.0, 0.1, 5.0));

    private final BoolSetting noClip = register(new BoolSetting(
            "No Clip", "Disable collision for the camera", true));

    private final BoolSetting renderPlayer = register(new BoolSetting(
            "Render Player", "Show player at original position while in freecam", true));

    // Saved player state
    private double savedX, savedY, savedZ;
    private float savedYaw, savedPitch;

    // Ghost camera entity
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

        double newX = cameraEntity.getX() + dx;
        double newY = cameraEntity.getY() + dy;
        double newZ = cameraEntity.getZ() + dz;

        cameraEntity.setPosition(newX, newY, newZ);

        // Sync camera yaw/pitch to player's look direction
        cameraEntity.setYaw(mc.player.getYaw());
        cameraEntity.setPitch(mc.player.getPitch());

        // Freeze player movement
        mc.player.setVelocity(Vec3d.ZERO);
        mc.player.setPosition(savedX, savedY, savedZ);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (!renderPlayer.isEnabled() || mc.player == null) return;

        // Draw a box at the player's original frozen position
        Box playerBox = new Box(
                savedX - 0.3, savedY, savedZ - 0.3,
                savedX + 0.3, savedY + 1.8, savedZ + 0.3
        );
        RenderUtil.drawESPBox(event.getMatrixStack(), playerBox, 0.2f, 0.8f, 0.2f, 0.9f, 1.5f);
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

        //? if mc >= "1.20.5" {
        @Override
        protected void initDataTracker(net.minecraft.entity.data.DataTracker.Builder builder) {}
        //?} else {
        /*@Override
        protected void initDataTracker() {}*/
        //?}

        @Override
        protected void readCustomDataFromNbt(net.minecraft.nbt.NbtCompound nbt) {}

        @Override
        protected void writeCustomDataToNbt(net.minecraft.nbt.NbtCompound nbt) {}
    }
}
