package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Phase extends Module {

    private final ModeSetting mode = register(new ModeSetting("Mode", "Phase mode", "Vanilla", "Vanilla", "Packet", "Gate"));

    private boolean alternate = false;

    public Phase() {
        super("Phase", "Pass through blocks by flickering positions", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        alternate = false;
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null || mc.world == null) return;
        if (!mode.is("Vanilla") && !mode.is("Gate")) return;

        Vec3d pos = mc.player.getPos();
        BlockPos blockPos = mc.player.getBlockPos();

        if (mode.is("Gate")) {
            boolean nearGate = false;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos check = blockPos.add(dx, 0, dz);
                    var state = mc.world.getBlockState(check);
                    if (state.getBlock() instanceof FenceGateBlock || state.getBlock() instanceof FenceBlock) {
                        nearGate = true;
                        break;
                    }
                }
                if (nearGate) break;
            }
            if (!nearGate) return;
        }

        boolean insideBlock = !mc.world.getBlockState(blockPos).isAir()
                || !mc.world.getBlockState(blockPos.up()).isAir();

        if (insideBlock) {
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            double pushX = -Math.sin(yaw) * 0.1;
            double pushZ = Math.cos(yaw) * 0.1;
            event.setX(event.getX() + pushX);
            event.setZ(event.getZ() + pushZ);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        Vec3d pos = mc.player.getPos();

        if (mode.is("Vanilla")) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y + 0.0625, pos.z, false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y, pos.z, false));
        } else if (mode.is("Packet")) {
            double yOffset = alternate ? 0.0625 : -0.0625;
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y + yOffset, pos.z, false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y, pos.z, false));
            alternate = !alternate;
        } else if (mode.is("Gate")) {
            if (mc.world == null) return;
            BlockPos blockPos = mc.player.getBlockPos();
            boolean nearGate = false;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos check = blockPos.add(dx, 0, dz);
                    var state = mc.world.getBlockState(check);
                    if (state.getBlock() instanceof FenceGateBlock || state.getBlock() instanceof FenceBlock) {
                        nearGate = true;
                        break;
                    }
                }
                if (nearGate) break;
            }
            if (nearGate) {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y + 0.0625, pos.z, false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y, pos.z, false));
            }
        }
    }
}
