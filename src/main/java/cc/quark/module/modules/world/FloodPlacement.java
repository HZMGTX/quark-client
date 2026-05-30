package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class FloodPlacement extends Module {

    private final ModeSetting pattern = register(new ModeSetting("Pattern", "Block placement pattern", "Line", "Line", "Square", "Circle"));
    private final IntSetting range = register(new IntSetting("Range", "Placement range from player", 3, 1, 8));
    private final IntSetting maxBlocks = register(new IntSetting("MaxBlocks", "Max blocks to place per session", 20, 1, 100));

    private int blocksPlaced = 0;

    public FloodPlacement() {
        super("FloodPlacement", "Places blocks (sand/gravel/dirt) outward from player in a pattern", Category.WORLD);
    }

    @Override
    public void onEnable() {
        blocksPlaced = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (blocksPlaced >= maxBlocks.get()) {
            setEnabled(false);
            return;
        }

        int slot = findBlockSlot();
        if (slot < 0) return;

        List<BlockPos> candidates = getCandidates();
        if (candidates.isEmpty()) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;

        for (BlockPos pos : candidates) {
            if (blocksPlaced >= maxBlocks.get()) break;
            if (!mc.world.getBlockState(pos).isAir()) continue;

            // Find a solid neighbor below or adjacent to place against
            BlockPos below = pos.down();
            if (!mc.world.getBlockState(below).isSolidBlock(mc.world, below)) continue;

            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(below).add(0, 0.5, 0), Direction.UP, below, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            blocksPlaced++;
        }

        mc.player.getInventory().selectedSlot = prevSlot;
    }

    private List<BlockPos> getCandidates() {
        BlockPos center = mc.player.getBlockPos();
        int r = range.get();
        List<BlockPos> list = new ArrayList<>();
        String pat = pattern.get();

        if (pat.equals("Line")) {
            // Line in facing direction
            double yaw = Math.toRadians(mc.player.getYaw());
            int dx = (int) Math.round(-Math.sin(yaw));
            int dz = (int) Math.round(Math.cos(yaw));
            for (int i = 1; i <= r; i++) {
                list.add(center.add(dx * i, 0, dz * i));
            }
        } else if (pat.equals("Square")) {
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    list.add(center.add(x, 0, z));
                }
            }
        } else { // Circle
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    if (x * x + z * z <= r * r) {
                        list.add(center.add(x, 0, z));
                    }
                }
            }
        }
        return list;
    }

    private int findBlockSlot() {
        if (mc.player == null) return -1;
        var inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            var stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() == Items.SAND
                    || stack.getItem() == Items.GRAVEL
                    || stack.getItem() == Items.DIRT) {
                return i;
            }
        }
        // Fallback: any block item in hotbar
        for (int i = 0; i < 9; i++) {
            var stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) return i;
        }
        return -1;
    }
}
