package com.ghostclient.module.modules.world;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.IntSetting;
import com.ghostclient.setting.ModeSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * AutoMine - automatically mines a specific block type in range.
 *
 * The player can set which block to mine via the Mode setting or by
 * using the .automine <block> command through the command system.
 *
 * Predefined modes:
 *   Diamond – mine diamond ore and deepslate diamond ore
 *   Gold    – gold ore variants
 *   Iron    – iron ore variants
 *   Logs    – all log types (useful for auto-woodcutting)
 *   Custom  – user-specified block ID set via setTargetBlock()
 */
public class AutoMine extends Module {

    private final ModeSetting blockMode = register(new ModeSetting(
            "Block", "Block type to mine", "Diamond",
            "Diamond", "Gold", "Iron", "Logs", "Custom"));

    private final IntSetting range = register(new IntSetting(
            "Range", "Search radius for target blocks", 4, 1, 8));

    private String customBlock = "minecraft:stone";

    private int breakTick = 0;
    private BlockPos currentTarget = null;

    public AutoMine() {
        super("AutoMine", "Auto-mines a specified block type in range", Category.WORLD);
    }

    public void setCustomBlock(String blockId) {
        this.customBlock = blockId;
    }

    @Override
    public void onEnable() {
        currentTarget = null;
        breakTick = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        BlockPos center = mc.player.getBlockPos();
        int r = range.get();

        // Find all matching blocks in range
        List<BlockPos> candidates = new ArrayList<>();
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (matchesMode(mc.world.getBlockState(pos).getBlock())) {
                        candidates.add(pos);
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            currentTarget = null;
            breakTick = 0;
            return;
        }

        // Pick nearest candidate
        BlockPos nearest = candidates.stream()
                .min(Comparator.comparingDouble(p ->
                        p.getSquaredDistance(new Vec3i(center.getX(), center.getY(), center.getZ()))))
                .orElse(null);

        if (nearest == null) return;

        // If target changed, reset break progress
        if (!nearest.equals(currentTarget)) {
            currentTarget = nearest;
            breakTick = 0;
        }

        // Rotate toward the target
        double dx = nearest.getX() + 0.5 - mc.player.getX();
        double dy = nearest.getY() + 0.5 - mc.player.getEyeY();
        double dz = nearest.getZ() + 0.5 - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        float yaw   = (float)Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float)-Math.toDegrees(Math.atan2(dy, dist));

        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);

        // Determine best face (just use whichever side is visible)
        Direction face = getClosestFace(center, nearest);

        if (breakTick == 0) {
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, nearest, face));
        }

        // Calculate break progress based on block hardness
        BlockState state = mc.world.getBlockState(nearest);
        float delta = state.calcBlockBreakingDelta(mc.player, mc.world, nearest);

        breakTick++;

        // When progress would reach 1.0 (block broken), send stop packet
        if (delta * breakTick >= 1.0f) {
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, nearest, face));
            mc.world.breakBlock(nearest, true, mc.player);
            currentTarget = null;
            breakTick = 0;
        }
    }

    private boolean matchesMode(Block block) {
        return switch (blockMode.get()) {
            case "Diamond" -> block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE;
            case "Gold"    -> block == Blocks.GOLD_ORE    || block == Blocks.DEEPSLATE_GOLD_ORE
                              || block == Blocks.NETHER_GOLD_ORE;
            case "Iron"    -> block == Blocks.IRON_ORE    || block == Blocks.DEEPSLATE_IRON_ORE;
            case "Logs"    -> block.getDefaultState().isIn(net.minecraft.registry.tag.BlockTags.LOGS);
            case "Custom"  -> Registries.BLOCK.getId(block).toString().equals(customBlock);
            default -> false;
        };
    }

    private Direction getClosestFace(BlockPos from, BlockPos to) {
        if (from.getY() < to.getY()) return Direction.DOWN;
        if (from.getY() > to.getY()) return Direction.UP;
        if (from.getX() < to.getX()) return Direction.WEST;
        if (from.getX() > to.getX()) return Direction.EAST;
        if (from.getZ() < to.getZ()) return Direction.NORTH;
        return Direction.SOUTH;
    }
}
