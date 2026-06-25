package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoCauldron extends Module {

    private final BoolSetting autoRefill = register(new BoolSetting("AutoRefill", "Automatically refill cauldron when empty using water bucket", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoCauldron() {
        super("AutoCauldron", "Uses cauldron with bucket to fill/empty water", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(400)) return;

        BlockPos center = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-3, -1, -3), center.add(3, 1, 3))) {
            var state = mc.world.getBlockState(pos);
            BlockPos immutable = pos.toImmutable();

            if (state.isOf(Blocks.WATER_CAULDRON)) {
                int level = state.get(LeveledCauldronBlock.LEVEL);
                if (level > 0) {
                    int bucketSlot = findItem(Items.BUCKET);
                    if (bucketSlot == -1) continue;
                    int saved = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = bucketSlot;
                    BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, immutable, false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    mc.player.getInventory().selectedSlot = saved;
                    timer.reset();
                    return;
                }
            }

            if (autoRefill.isEnabled() && state.isOf(Blocks.CAULDRON)) {
                int waterBucketSlot = findItem(Items.WATER_BUCKET);
                if (waterBucketSlot == -1) continue;
                int saved = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = waterBucketSlot;
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, immutable, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.player.getInventory().selectedSlot = saved;
                timer.reset();
                return;
            }
        }
    }

    private int findItem(net.minecraft.item.Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }
}
