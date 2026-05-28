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

public class Teleport extends Module {

    private final IntSetting key = register(new IntSetting("Key", "Key to trigger teleport (GLFW keycode)", GLFW.GLFW_KEY_F7, GLFW.GLFW_KEY_F1, GLFW.GLFW_KEY_F12));
    private final DoubleSetting distance = register(new DoubleSetting("Distance", "Teleport distance in blocks", 5.0, 1.0, 50.0));
    private final BoolSetting checkBlocks = register(new BoolSetting("Check Blocks", "Avoid teleporting into walls", true));

    public Teleport() {
        super("Teleport", "Teleport forward quickly on key press", Category.MOVEMENT);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (event.getKeyCode() != key.get()) return;

        double yawRad = Math.toRadians(mc.player.getYaw());
        double pitchRad = Math.toRadians(mc.player.getPitch());

        double dx = -Math.sin(yawRad) * Math.cos(pitchRad);
        double dy = -Math.sin(pitchRad);
        double dz = Math.cos(yawRad) * Math.cos(pitchRad);

        double dist = distance.get();

        if (checkBlocks.isEnabled() && mc.world != null) {
            for (double step = 1.0; step <= dist; step += 0.5) {
                double tx = mc.player.getX() + dx * step;
                double ty = mc.player.getY() + dy * step;
                double tz = mc.player.getZ() + dz * step;
                BlockPos feetPos = new BlockPos((int) Math.floor(tx), (int) Math.floor(ty), (int) Math.floor(tz));
                BlockPos headPos = feetPos.up();
                if (!mc.world.getBlockState(feetPos).isAir() || !mc.world.getBlockState(headPos).isAir()) {
                    dist = step - 0.5;
                    break;
                }
            }
        }

        double nx = mc.player.getX() + dx * dist;
        double ny = mc.player.getY() + dy * dist;
        double nz = mc.player.getZ() + dz * dist;

        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(nx, ny, nz, mc.player.isOnGround()));
        mc.player.setPosition(nx, ny, nz);
    }
}
