package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

/**
 * Teleport - on key press, raycast along crosshair direction; CheckBlocks
 * validates the path by scanning 0.5-block steps and stopping before any
 * solid block; sends PlayerMoveC2SPacket to the new position and updates
 * client-side position.
 */
public class Teleport extends Module {

    private final IntSetting key = register(new IntSetting(
            "Key", "GLFW key code to trigger teleport", GLFW.GLFW_KEY_F7, GLFW.GLFW_KEY_F1, GLFW.GLFW_KEY_F12));
    private final DoubleSetting distance = register(new DoubleSetting(
            "Distance", "Maximum teleport distance in blocks", 10.0, 1.0, 50.0));
    private final BoolSetting checkBlocks = register(new BoolSetting(
            "Check Blocks", "Stop before solid blocks along the path", true));
    private final BoolSetting horizontal = register(new BoolSetting(
            "Horizontal Only", "Ignore pitch — teleport horizontally only", false));

    public Teleport() {
        super("Teleport", "Teleport to crosshair target on key press with block collision check", Category.MOVEMENT);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (event.getKeyCode() != key.get()) return;

        double yawRad   = Math.toRadians(mc.player.getYaw());
        double pitchRad = horizontal.isEnabled() ? 0.0 : Math.toRadians(mc.player.getPitch());

        double dx = -Math.sin(yawRad) * Math.cos(pitchRad);
        double dy = horizontal.isEnabled() ? 0.0 : -Math.sin(pitchRad);
        double dz =  Math.cos(yawRad) * Math.cos(pitchRad);

        double maxDist = distance.get();
        double actualDist = maxDist;

        if (checkBlocks.isEnabled() && mc.world != null) {
            for (double step = 0.5; step <= maxDist; step += 0.5) {
                double tx = mc.player.getX() + dx * step;
                double ty = mc.player.getY() + dy * step;
                double tz = mc.player.getZ() + dz * step;

                BlockPos feetPos = BlockPos.ofFloored(tx, ty, tz);
                BlockPos headPos = feetPos.up();

                boolean feetBlocked = !mc.world.getBlockState(feetPos).isAir();
                boolean headBlocked = !mc.world.getBlockState(headPos).isAir();

                if (feetBlocked || headBlocked) {
                    actualDist = Math.max(0, step - 0.5);
                    break;
                }
            }
        }

        double nx = mc.player.getX() + dx * actualDist;
        double ny = mc.player.getY() + dy * actualDist;
        double nz = mc.player.getZ() + dz * actualDist;

        // Send packet first, then update client position
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                nx, ny, nz, mc.player.isOnGround()));
        mc.player.setPosition(nx, ny, nz);
    }
}
