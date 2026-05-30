package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * GhostMode - sends a burst of position packets with offsets to ghost through
 * thin walls. Self-disables after one activation.
 */
public class GhostMode extends Module {

    private final DoubleSetting distance = register(new DoubleSetting(
            "Distance", "How far to project ghost packets (blocks)", 1.0, 0.5, 3.0));
    private final ModeSetting direction = register(new ModeSetting(
            "Direction", "Direction to send ghost packets", "Forward",
            "Forward", "Backward", "Up", "Down"));

    private boolean activated = false;

    public GhostMode() {
        super("GhostMode", "Send position-offset packets to ghost through thin walls", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        activated = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (activated) {
            disable();
            return;
        }

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        double dist = distance.get();

        double yawRad = Math.toRadians(mc.player.getYaw());
        double dirX = 0, dirY = 0, dirZ = 0;

        switch (direction.get()) {
            case "Forward" -> {
                dirX = -Math.sin(yawRad) * dist;
                dirZ = Math.cos(yawRad) * dist;
            }
            case "Backward" -> {
                dirX = Math.sin(yawRad) * dist;
                dirZ = -Math.cos(yawRad) * dist;
            }
            case "Up" -> dirY = dist;
            case "Down" -> dirY = -dist;
        }

        // Send stepping packets towards target
        int steps = 8;
        for (int i = 1; i <= steps; i++) {
            double frac = (double) i / steps;
            mc.getNetworkHandler().sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(
                            x + dirX * frac,
                            y + dirY * frac,
                            z + dirZ * frac,
                            false));
        }

        // Send return packets
        for (int i = steps - 1; i >= 0; i--) {
            double frac = (double) i / steps;
            mc.getNetworkHandler().sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(
                            x + dirX * frac,
                            y + dirY * frac,
                            z + dirZ * frac,
                            mc.player.isOnGround()));
        }

        activated = true;
    }
}
