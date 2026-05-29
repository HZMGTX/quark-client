package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * RubberBand - every N seconds, teleport player back by BackDist blocks then
 * immediately forward again (simulate lag rubber-band). Testing tool.
 */
public class RubberBand extends Module {

    private final DoubleSetting interval = register(new DoubleSetting(
            "Interval", "Seconds between rubber-band triggers", 3.0, 0.5, 10.0));
    private final DoubleSetting backDist = register(new DoubleSetting(
            "Back Dist", "Blocks to teleport back", 5.0, 1.0, 20.0));

    private final TimerUtil timer = new TimerUtil();

    public RubberBand() {
        super("RubberBand", "Periodically simulate lag rubber-band for testing; testing tool only", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (!timer.hasReached(interval.get() * 1000)) return;
        timer.reset();

        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        double dx = -Math.sin(yawRad);
        double dz =  Math.cos(yawRad);
        double dist = backDist.get();

        // Send back position
        double bx = mc.player.getX() - dx * dist;
        double by = mc.player.getY();
        double bz = mc.player.getZ() - dz * dist;
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                bx, by, bz, mc.player.isOnGround()));

        // Immediately send forward position (simulate snap-back)
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
    }
}
