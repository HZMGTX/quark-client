package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

/**
 * ClipWalk - phases through blocks using rapid position packets.
 * Sends alternating Y-offset packets to confuse the server's collision
 * detection and clip the player through thin walls / floors.
 */
public class ClipWalk extends Module {

    private final DoubleSetting distance = register(new DoubleSetting(
            "Distance", "How far to clip per activation (blocks)", 1.0, 0.1, 3.0));
    private final ModeSetting direction = register(new ModeSetting(
            "Direction", "Clip direction", "Forward", "Forward", "Backward", "Up", "Down"));

    public ClipWalk() {
        super("ClipWalk", "Phase through blocks using rapid position packets", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        Vec3d pos = mc.player.getPos();
        double dist = distance.get();

        double dx = 0, dy = 0, dz = 0;
        float yaw = (float) Math.toRadians(mc.player.getYaw());

        switch (direction.get()) {
            case "Forward"  -> { dx = -Math.sin(yaw) * dist; dz = Math.cos(yaw) * dist; }
            case "Backward" -> { dx =  Math.sin(yaw) * dist; dz = -Math.cos(yaw) * dist; }
            case "Up"       -> dy =  dist;
            case "Down"     -> dy = -dist;
        }

        // Send two pairs of position packets to clip through the block boundary
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                pos.x + dx * 0.5, pos.y + dy * 0.5, pos.z + dz * 0.5, false));
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                pos.x + dx, pos.y + dy, pos.z + dz, false));

        mc.player.setPos(pos.x + dx, pos.y + dy, pos.z + dz);
        mc.player.setVelocity(0, 0, 0);

        // Self-disable after one clip activation
        disable();
    }
}
