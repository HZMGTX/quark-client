package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * FreeCam (render variant) - detaches the camera from the player and lets
 * it fly freely. The player entity is frozen on the server while this is
 * active. WASD + space/shift move the camera.
 */
public class FreeCam extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Camera movement speed", 1.0, 0.1, 5.0));

    private final BoolSetting noClip = register(new BoolSetting(
            "No Clip", "Pass through walls (camera only)", true));

    private double savedX, savedY, savedZ;
    private float  savedYaw, savedPitch;
    private FreeCamEntity cameraEntity;
    private Entity originalCamera;

    public FreeCam() {
        super("FreeCam", "Detach camera from player and fly it freely", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) { disable(); return; }

        savedX     = mc.player.getX();
        savedY     = mc.player.getY();
        savedZ     = mc.player.getZ();
        savedYaw   = mc.player.getYaw();
        savedPitch = mc.player.getPitch();

        cameraEntity = new FreeCamEntity(mc.world, savedX, savedY, savedZ, savedYaw, savedPitch);
        originalCamera = mc.getCameraEntity();
        mc.setCameraEntity(cameraEntity);
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (originalCamera != null) mc.setCameraEntity(originalCamera);
        cameraEntity = null;
        mc.player.setPosition(savedX, savedY, savedZ);
        mc.player.setYaw(savedYaw);
        mc.player.setPitch(savedPitch);
        mc.player.setVelocity(Vec3d.ZERO);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || cameraEntity == null) return;

        Input input = mc.player.input;
        float yaw   = cameraEntity.getYaw();
        float pitch = cameraEntity.getPitch();
        double spd  = speed.get() * 0.1;

        double dx = 0, dy = 0, dz = 0;

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
        if (input.pressingLeft)  { dx -= Math.cos(Math.toRadians(yaw)) * spd; dz -= Math.sin(Math.toRadians(yaw)) * spd; }
        if (input.pressingRight) { dx += Math.cos(Math.toRadians(yaw)) * spd; dz += Math.sin(Math.toRadians(yaw)) * spd; }
        if (input.jumping)  dy += spd;
        if (input.sneaking) dy -= spd;

        cameraEntity.setPosition(cameraEntity.getX() + dx, cameraEntity.getY() + dy, cameraEntity.getZ() + dz);
        cameraEntity.setYaw(mc.player.getYaw());
        cameraEntity.setPitch(mc.player.getPitch());

        // Keep the real player frozen in place
        mc.player.setVelocity(Vec3d.ZERO);
        mc.player.setPosition(savedX, savedY, savedZ);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            event.cancel();
        }
    }

    // ---- ghost camera entity ----

    public static class FreeCamEntity extends Entity {

        public FreeCamEntity(World world, double x, double y, double z, float yaw, float pitch) {
            super(EntityType.ITEM, world);
            setPosition(x, y, z);
            setYaw(yaw);
            setPitch(pitch);
        }

        @Override
        protected void initDataTracker(DataTracker.Builder builder) {}

        @Override
        protected void readCustomDataFromNbt(NbtCompound nbt) {}

        @Override
        protected void writeCustomDataToNbt(NbtCompound nbt) {}
    }
}
