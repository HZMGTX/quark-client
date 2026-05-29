package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

/**
 * Skip - send a teleport packet ahead of actual position by Distance blocks
 * along the current movement direction each tick, giving a "skip" glitch effect.
 */
public class Skip extends Module {

    private final DoubleSetting distance = register(new DoubleSetting(
            "Distance", "Blocks to skip ahead per trigger", 3.0, 0.5, 10.0));
    private final DoubleSetting interval = register(new DoubleSetting(
            "Interval", "Ticks between skips", 10.0, 1.0, 40.0));

    private int tickCount = 0;

    public Skip() {
        super("Skip", "Periodically send a position packet ahead of actual movement", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        tickCount = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        tickCount++;
        if (tickCount < (int) interval.get()) return;
        tickCount = 0;

        double yawRad = Math.toRadians(mc.player.getYaw());
        double len = Math.sqrt(fwd * fwd + side * side);
        double normFwd  = fwd  / len;
        double normSide = side / len;

        double dist = distance.get();
        double dx = (-Math.sin(yawRad) * normFwd + Math.cos(yawRad) * normSide) * dist;
        double dz = ( Math.cos(yawRad) * normFwd + Math.sin(yawRad) * normSide) * dist;

        double nx = mc.player.getX() + dx;
        double ny = mc.player.getY();
        double nz = mc.player.getZ() + dz;

        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                nx, ny, nz, mc.player.isOnGround()));
    }
}
