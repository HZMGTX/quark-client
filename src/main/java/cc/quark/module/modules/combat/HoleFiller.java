package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * HoleFiller - fills single-block holes (1x1 pits) near enemy players to remove their safety spots.
 */
public class HoleFiller extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to scan for holes to fill", 4.0, 2.0, 6.0));

    private final ModeSetting blockMode = register(new ModeSetting(
            "Block", "Which block type to use for filling", "Obsidian",
            "Obsidian", "Cobblestone", "Any"));

    private final BoolSetting onlyHoles = register(new BoolSetting(
            "Only Holes", "Only fill single-block holes surrounded by solid blocks on all sides", true));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between fill actions", 100, 0, 500));

    private final TimerUtil timer = new TimerUtil();
    private int blocksFilled = 0;

    public HoleFiller() {
        super("HoleFiller", "Fills nearby holes to deny enemy safety spots", Category.COMBAT);
    }

    @Override
    public String getSuffix() {
        return blocksFilled + " filled";
    }

    @Override
    public void onEnable() {
        timer.reset();
        blocksFilled = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Find a suitable block in hotbar
        int blockSlot = findBlockSlot();
        if (blockSlot == -1) return;

        // Find holes within range
        List<BlockPos> holes = findHoles();
        if (holes.isEmpty()) return;

        // Fill the closest hole
        BlockPos holePos = holes.get(0);
        double closest = Double.MAX_VALUE;
        for (BlockPos pos : holes) {
            double dist = mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));
            if (dist < closest) {
                closest = dist;
                holePos = pos;
            }
        }

        // Ensure we have the block selected
        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        // Place block in the hole by interacting with the bottom face of the hole
        // The support block is below the hole
        BlockPos supportPos = holePos.down();
        Vec3d hitVec = Vec3d.ofCenter(supportPos).add(0, 0.5, 0);
        BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, supportPos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
        blocksFilled++;

        mc.player.getInventory().selectedSlot = prevSlot;
        timer.reset();
    }

    private List<BlockPos> findHoles() {
        List<BlockPos> holes = new ArrayList<>();
        BlockPos playerPos = mc.player.getBlockPos();
        int r = (int) Math.ceil(range.get());

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -2; dy <= 1; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = playerPos.add(dx, dy, dz);
                    double dist = mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));
                    if (dist > range.get()) continue;

                    if (!mc.world.getBlockState(pos).isAir()) continue;

                    if (onlyHoles.isEnabled()) {
                        // Must be a proper hole: air block with solid surroundings on all sides and below
                        if (!isSingleBlockHole(pos)) continue;
                    } else {
                        // Just needs a solid block below
                        if (!mc.world.getBlockState(pos.down()).isSolidBlock(mc.world, pos.down())) continue;
                    }

                    holes.add(pos);
                }
            }
        }
        return holes;
    }

    private boolean isSingleBlockHole(BlockPos pos) {
        // Solid below
        if (!mc.world.getBlockState(pos.down()).isSolidBlock(mc.world, pos.down())) return false;
        // All 4 cardinal sides must be non-air
        Direction[] sides = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (Direction dir : sides) {
            if (mc.world.getBlockState(pos.offset(dir)).isAir()) return false;
        }
        return true;
    }

    private int findBlockSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            switch (blockMode.get()) {
                case "Obsidian":
                    if (stack.getItem() == Items.OBSIDIAN) return i;
                    break;
                case "Cobblestone":
                    if (stack.getItem() == Items.COBBLESTONE) return i;
                    break;
                case "Any":
                    // Accept obsidian, cobblestone, or any block-like item
                    if (stack.getItem() == Items.OBSIDIAN
                            || stack.getItem() == Items.COBBLESTONE
                            || stack.getItem() == Items.STONE
                            || stack.getItem() == Items.DIRT
                            || stack.getItem() == Items.NETHERRACK) {
                        return i;
                    }
                    break;
            }
        }
        return -1;
    }
}
