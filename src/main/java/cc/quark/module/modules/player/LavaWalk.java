package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * LavaWalk — allows the player to traverse lava by placing blocks or pouring
 * water onto lava tiles ahead of the player's movement path.
 */
public class LavaWalk extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "What to place on lava to traverse it",
            "Cobblestone", "Cobblestone", "Water"));

    private final BoolSetting onlyMoving = register(new BoolSetting(
            "Only Moving", "Only place blocks while actually moving", true));

    private final BoolSetting onlyGround = register(new BoolSetting(
            "Only On Ground", "Only place blocks when on the ground", true));

    private final TimerUtil timer = new TimerUtil();

    public LavaWalk() {
        super("LavaWalk", "Places blocks/water to cross lava", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        ClientPlayerEntity player = mc.player;

        if (onlyGround.isEnabled() && !player.isOnGround()) return;
        if (!timer.hasReached(100)) return;

        boolean moving = player.getVelocity().x != 0 || player.getVelocity().z != 0;
        if (onlyMoving.isEnabled() && !moving) return;
        timer.reset();

        // Check the block directly below the player and the two blocks ahead in movement direction
        Vec3d velocity = player.getVelocity();
        Vec3d look = player.getRotationVec(1.0f);
        // Use movement direction, fall back to look direction
        double moveX = Math.abs(velocity.x) > 0.01 ? velocity.x : look.x;
        double moveZ = Math.abs(velocity.z) > 0.01 ? velocity.z : look.z;

        BlockPos playerPos = player.getBlockPos();

        for (int step = 0; step <= 2; step++) {
            double factor = step * 0.6;
            int checkX = (int) Math.floor(player.getX() + moveX * factor);
            int checkZ = (int) Math.floor(player.getZ() + moveZ * factor);
            BlockPos checkPos = new BlockPos(checkX, playerPos.getY() - 1, checkZ);

            if (mc.world.getBlockState(checkPos).getBlock() == Blocks.LAVA) {
                if (mode.get().equals("Water")) {
                    placeWater(player, checkPos);
                } else {
                    placeBlock(player, checkPos);
                }
                return;
            }
        }
    }

    private void placeBlock(ClientPlayerEntity player, BlockPos lavaPos) {
        // Find a cobblestone or stone block in hotbar
        int slot = findBlockSlot();
        if (slot == -1) return;

        int prev = player.getInventory().selectedSlot;
        player.getInventory().selectedSlot = slot;

        // Place on the top face of lava (treating lava surface as a placeable face)
        Vec3d hitVec = new Vec3d(lavaPos.getX() + 0.5, lavaPos.getY() + 1.0, lavaPos.getZ() + 0.5);
        BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, lavaPos, false);
        mc.interactionManager.interactBlock(player, Hand.MAIN_HAND, hit);
        player.swingHand(Hand.MAIN_HAND);
        player.getInventory().selectedSlot = prev;
    }

    private void placeWater(ClientPlayerEntity player, BlockPos lavaPos) {
        int slot = findWaterBucketSlot();
        if (slot == -1) return;

        int prev = player.getInventory().selectedSlot;
        player.getInventory().selectedSlot = slot;

        Vec3d hitVec = new Vec3d(lavaPos.getX() + 0.5, lavaPos.getY() + 1.0, lavaPos.getZ() + 0.5);
        BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, lavaPos, false);
        mc.interactionManager.interactBlock(player, Hand.MAIN_HAND, hit);
        player.swingHand(Hand.MAIN_HAND);
        player.getInventory().selectedSlot = prev;
    }

    private int findBlockSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem bi)) continue;
            Block b = bi.getBlock();
            // Prefer non-falling solid blocks; exclude lava/water/sand/gravel
            if (b == Blocks.COBBLESTONE || b == Blocks.STONE || b == Blocks.DIRT
                    || b == Blocks.COBBLED_DEEPSLATE || b == Blocks.NETHERRACK) {
                return i;
            }
        }
        // Fallback: any block item
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) return i;
        }
        return -1;
    }

    private int findWaterBucketSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.WATER_BUCKET) return i;
        }
        return -1;
    }
}
