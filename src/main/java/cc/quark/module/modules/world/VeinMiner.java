package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventBlockBreak;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ToolItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class VeinMiner extends Module {

    private final IntSetting maxBlocks = register(new IntSetting(
            "Max Blocks", "Maximum blocks to break in one vein", 16, 1, 64));

    private final BoolSetting onlyTools = register(new BoolSetting(
            "Only Tools", "Only activate when an appropriate tool is held", true));

    public VeinMiner() {
        super("VeinMiner", "Breaks all connected blocks of the same type when mining one", Category.WORLD);
    }

    @EventHandler
    public void onBlockBreak(EventBlockBreak event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (onlyTools.isEnabled()) {
            var stack = mc.player.getMainHandStack();
            if (stack.isEmpty() || !(stack.getItem() instanceof ToolItem)) return;
        }

        BlockPos origin = event.getPos();
        BlockState originState = event.getState();
        Block targetBlock = originState.getBlock();
        if (originState.isAir()) return;

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(origin);
        int count = 0;

        while (!queue.isEmpty() && count < maxBlocks.get()) {
            BlockPos pos = queue.poll();
            if (!pos.equals(origin)) {
                mc.interactionManager.attackBlock(pos, Direction.UP);
                mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
            }
            count++;
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.offset(dir);
                if (visited.contains(neighbor)) continue;
                if (mc.world.getBlockState(neighbor).getBlock() != targetBlock) continue;
                visited.add(neighbor);
                queue.add(neighbor);
            }
        }
    }

}

