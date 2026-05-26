package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * Teleport - sends position packets to shift the player a short distance forward
 * in their look direction when the jump key is pressed.
 */
public class Teleport extends Module {

    private final DoubleSetting distance = register(new DoubleSetting(
            "Distance", "Teleport distance in blocks", 3.0, 1.0, 8.0));

    public Teleport() {
        super("Teleport", "Short forward teleport", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.getNetworkHandler() == null) return;
        if (!mc.options.jumpKey.isPressed()) return;
        double yawRad = Math.toRadians(mc.player.getYaw());
        double dx = -Math.sin(yawRad) * distance.get();
        double dz = Math.cos(yawRad) * distance.get();
        double nx = mc.player.getX() + dx;
        double ny = mc.player.getY();
        double nz = mc.player.getZ() + dz;
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                nx, ny, nz, mc.player.isOnGround()));
        mc.player.setPosition(nx, ny, nz);
    }
}
