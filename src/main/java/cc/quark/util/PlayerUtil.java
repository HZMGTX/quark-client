package cc.quark.util;

import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Utility methods for querying the state of the local player.
 */
public final class PlayerUtil {

    private PlayerUtil() {}

    private static final MinecraftClient MC = MinecraftClient.getInstance();

    // -------------------------------------------------------------------------
    // Movement
    // -------------------------------------------------------------------------

    /**
     * Returns true when the player is pressing any movement key (WASD).
     */
    public static boolean isMoving() {
        MinecraftClient mc = MC;
        if (mc.player == null) return false;
        return mc.options.forwardKey.isPressed()
                || mc.options.backKey.isPressed()
                || mc.options.leftKey.isPressed()
                || mc.options.rightKey.isPressed();
    }

    /**
     * Horizontal speed of the player in blocks per tick (XZ plane).
     */
    public static double getSpeed() {
        if (MC.player == null) return 0.0;
        Vec3d vel = MC.player.getVelocity();
        return Math.sqrt(vel.x * vel.x + vel.z * vel.z);
    }

    /**
     * Total speed of the player (including vertical component).
     */
    public static double getSpeedTotal() {
        if (MC.player == null) return 0.0;
        return MC.player.getVelocity().length();
    }

    // -------------------------------------------------------------------------
    // Environment queries
    // -------------------------------------------------------------------------

    /**
     * Returns true when the player is standing in water or lava.
     */
    public static boolean isInLiquid() {
        ClientPlayerEntity player = MC.player;
        if (player == null) return false;
        return player.isTouchingWater() || player.isInLava();
    }

    /**
     * Returns true when the player is currently submerged in water (not just touching).
     */
    public static boolean isUnderwater() {
        if (MC.player == null) return false;
        return MC.player.isSubmergedIn(FluidTags.WATER);
    }

    /**
     * Returns true when the player is on the ground.
     */
    public static boolean isOnGround() {
        if (MC.player == null) return false;
        return MC.player.isOnGround();
    }

    /**
     * Returns true when the player is currently sprinting.
     */
    public static boolean isSprinting() {
        if (MC.player == null) return false;
        return MC.player.isSprinting();
    }

    // -------------------------------------------------------------------------
    // Block/safety queries
    // -------------------------------------------------------------------------

    /**
     * Check whether it is safe for the player to walk onto the block at the given
     * position â€” i.e. the block is solid and the two blocks above it are passable.
     *
     * @param pos block position to check (the block the player would stand ON)
     * @return true if safe to walk on
     */
    public static boolean isSafeToWalk(BlockPos pos) {
        World world = MC.world;
        if (world == null) return false;

        BlockState ground = world.getBlockState(pos);
        BlockState feet   = world.getBlockState(pos.up());
        BlockState head   = world.getBlockState(pos.up(2));

        // Ground must be solid, feet and head blocks must be passable.
        return ground.isSolidBlock(world, pos)
                && !feet.isSolidBlock(world, pos.up())
                && !head.isSolidBlock(world, pos.up(2));
    }

    /**
     * Returns true when the block directly below the player is solid
     * (i.e., the player is standing on solid ground with no void below).
     */
    public static boolean isAboveSolid() {
        if (MC.player == null || MC.world == null) return false;
        BlockPos below = MC.player.getBlockPos().down();
        BlockState state = MC.world.getBlockState(below);
        return state.isSolidBlock(MC.world, below);
    }

    /**
     * Returns true when any of the blocks immediately surrounding the player's
     * feet position contain a liquid (water or lava).
     */
    public static boolean isNearLiquid() {
        if (MC.player == null || MC.world == null) return false;
        BlockPos pos = MC.player.getBlockPos();
        for (BlockPos neighbor : new BlockPos[]{
                pos.north(), pos.south(), pos.east(), pos.west(), pos.down()
        }) {
            FluidState fluid = MC.world.getFluidState(neighbor);
            if (!fluid.isEmpty()) return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Health / hunger
    // -------------------------------------------------------------------------

    /**
     * Returns the local player's current health (0â€“20).
     */
    public static float getHealth() {
        if (MC.player == null) return 0f;
        return MC.player.getHealth();
    }

    /**
     * Returns the local player's current food level (0â€“20).
     */
    public static int getFoodLevel() {
        if (MC.player == null) return 0;
        return MC.player.getHungerManager().getFoodLevel();
    }
}
