package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class CropFarm extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Scan radius", 5, 1, 10));
    private final BoolSetting replant = register(new BoolSetting("Replant", "Replant held seeds on empty farmland", true));

    private int ticker = 0;

    public CropFarm() {
        super("CropFarm", "Harvests mature crops and replants on farmland", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (++ticker < 4) return;
        ticker = 0;

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            BlockState state = mc.world.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof CropBlock crop && crop.isMature(state)) {
                mc.interactionManager.attackBlock(pos.toImmutable(), Direction.UP);
                return;
            }
            if (replant.isEnabled() && state.isOf(Blocks.FARMLAND) && mc.world.getBlockState(pos.up()).isAir()
                    && mc.player.getMainHandStack().getItem() instanceof BlockItem) {
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.toImmutable(), false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                return;
            }
        }
    }
}
