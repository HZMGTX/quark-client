package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoSmelt extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Scan radius for furnaces", 4, 1, 8));
    private final IntSetting delay = register(new IntSetting("Delay", "Ticks between interactions", 10, 1, 40));

    private int ticker = 0;

    public AutoSmelt() {
        super("AutoSmelt", "Opens nearby furnaces to manage smelting", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (++ticker < delay.get()) return;
        ticker = 0;

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            if (!mc.world.getBlockState(pos).isOf(Blocks.FURNACE)) continue;
            BlockEntity be = mc.world.getBlockEntity(pos);
            if (!(be instanceof FurnaceBlockEntity)) continue;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            return;
        }
    }
}
