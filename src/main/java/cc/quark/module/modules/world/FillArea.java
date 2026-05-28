package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * FillArea — fills air blocks in a cube around the player with a configurable block.
 *
 * Settings:
 *   Radius — cube half-size.
 *   Block  — which block type to place (Stone / Dirt / Sand / Cobblestone / Any).
 *            "Any" uses whatever block item is currently in the main hand.
 *   Delay  — milliseconds between placements.
 */
public class FillArea extends Module {

    private final IntSetting  radius = register(new IntSetting("Radius", "Fill radius",         5,   1, 20));
    private final ModeSetting block  = register(new ModeSetting("Block", "Block type to place",
            "Any", "Any", "Stone", "Dirt", "Sand", "Cobblestone"));
    private final IntSetting  delay  = register(new IntSetting("Delay",  "Milliseconds between placements", 200, 0, 500));

    private final TimerUtil timer = new TimerUtil();
    private int placed = 0;

    public FillArea() {
        super("FillArea", "Fills air gaps around you with the selected block.", Category.WORLD);
    }

    @Override
    public void onEnable() {
        placed = 0;
        timer.reset();
    }

    @Override
    public String getSuffix() {
        return "Placed: " + placed;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;
        timer.reset();

        // Select or verify block in hotbar
        if (!block.is("Any")) {
            int slot = findBlockInHotbar(getTargetBlock());
            if (slot == -1) return; // don't have the block
            if (slot != mc.player.getInventory().selectedSlot)
                mc.player.getInventory().selectedSlot = slot;
        } else {
            // Must be holding a block item
            if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;
        }

        int r = radius.get();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(
                center.add(-r, -r, -r),
                center.add( r,  r,  r))) {

            if (!mc.world.getBlockState(pos).isAir()) continue;

            // Find an adjacent solid block to place against
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.offset(dir);
                if (mc.world.getBlockState(neighbor).isAir()) continue;
                if (mc.world.getBlockState(neighbor).getHardness(mc.world, neighbor) < 0) continue;

                BlockHitResult hit = new BlockHitResult(
                        Vec3d.ofCenter(neighbor),
                        dir.getOpposite(),
                        neighbor.toImmutable(),
                        false);

                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                placed++;
                return;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Block getTargetBlock() {
        return switch (block.get()) {
            case "Stone"      -> Blocks.STONE;
            case "Dirt"       -> Blocks.DIRT;
            case "Sand"       -> Blocks.SAND;
            case "Cobblestone"-> Blocks.COBBLESTONE;
            default           -> null;
        };
    }

    private int findBlockInHotbar(Block target) {
        if (mc.player == null || target == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem bi && bi.getBlock() == target)
                return i;
        }
        return -1;
    }
}
