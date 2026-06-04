package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BlockSwapper extends Module {

    private final StringSetting from = register(new StringSetting(
            "From", "Block to replace (registry name, e.g. dirt)", "dirt"));
    private final StringSetting to = register(new StringSetting(
            "To", "Block to place instead (registry name, e.g. grass_block)", "grass_block"));

    private final TimerUtil timer = new TimerUtil();
    private static final int RADIUS = 4;

    public BlockSwapper() {
        super("BlockSwapper", "Swaps specific blocks with replacement blocks in a radius", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(200)) return;
        timer.reset();

        Block targetBlock = getBlock(from.get());
        Block replaceBlock = getBlock(to.get());
        if (targetBlock == null || replaceBlock == null) return;

        // Find replacement item in hotbar
        net.minecraft.item.Item replaceItem = replaceBlock.asItem();
        int replaceSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(replaceItem)) {
                replaceSlot = i;
                break;
            }
        }
        if (replaceSlot == -1) return;

        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-RADIUS, -2, -RADIUS), center.add(RADIUS, 2, RADIUS))) {
            if (!mc.world.getBlockState(pos).isOf(targetBlock)) continue;

            // Break target block
            mc.interactionManager.attackBlock(pos.toImmutable(), Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);

            // Place replacement on the block below
            BlockPos below = pos.down();
            if (mc.world.getBlockState(below).isSolidBlock(mc.world, below)) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = replaceSlot;
                Vec3d hitVec = Vec3d.ofCenter(below).add(0, 0.5, 0);
                BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, below.toImmutable(), false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.getInventory().selectedSlot = prev;
            }
            return;
        }
    }

    private Block getBlock(String name) {
        try {
            String id = name.trim().toLowerCase();
            if (!id.contains(":")) id = "minecraft:" + id;
            return Registries.BLOCK.get(new Identifier(id));
        } catch (Exception e) {
            return null;
        }
    }
}
