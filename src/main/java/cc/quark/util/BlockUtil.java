package cc.quark.util;

import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class BlockUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isAir(BlockPos pos) {
        if (mc.world == null) return true;
        return mc.world.getBlockState(pos).isAir();
    }

    public static boolean isSolid(BlockPos pos) {
        if (mc.world == null) return false;
        return mc.world.getBlockState(pos).isSolidBlock(mc.world, pos);
    }

    public static boolean isReplaceable(BlockPos pos) {
        if (mc.world == null) return false;
        return mc.world.getBlockState(pos).isReplaceable();
    }

    public static boolean canPlaceBlockAt(BlockPos pos) {
        if (mc.world == null) return false;
        return isAir(pos) || isReplaceable(pos);
    }

    public static List<BlockPos> getBlocksInRadius(BlockPos center, int radius) {
        List<BlockPos> list = new ArrayList<>();
        for (int x = -radius; x <= radius; x++)
            for (int y = -radius; y <= radius; y++)
                for (int z = -radius; z <= radius; z++)
                    list.add(center.add(x, y, z));
        return list;
    }

    public static boolean isOre(Block block) {
        return block instanceof ExperienceDroppingBlock ||
               block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE ||
               block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE ||
               block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE ||
               block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE ||
               block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE ||
               block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE ||
               block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE ||
               block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE ||
               block == Blocks.NETHER_QUARTZ_ORE || block == Blocks.NETHER_GOLD_ORE ||
               block == Blocks.ANCIENT_DEBRIS;
    }

    public static float getHardness(BlockPos pos) {
        if (mc.world == null) return 0f;
        return mc.world.getBlockState(pos).getHardness(mc.world, pos);
    }

    public static Direction getPlaceFace(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.offset(dir);
            if (isSolid(neighbor)) return dir.getOpposite();
        }
        return Direction.DOWN;
    }

    public static boolean isVoid(BlockPos pos) {
        if (mc.world == null) return false;
        return pos.getY() < mc.world.getBottomY();
    }

    public static boolean isBedrock(BlockPos pos) {
        if (mc.world == null) return false;
        return mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK;
    }

    public static boolean isObsidian(BlockPos pos) {
        if (mc.world == null) return false;
        Block b = mc.world.getBlockState(pos).getBlock();
        return b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN;
    }

    public static boolean isHole(BlockPos pos) {
        if (mc.world == null) return false;
        if (!isAir(pos)) return false;
        BlockPos below = pos.down();
        boolean okBelow = isSolid(below) || isBedrock(below) || isObsidian(below);
        if (!okBelow) return false;
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos side = pos.offset(dir);
            if (isAir(side)) return false;
        }
        return true;
    }
}
