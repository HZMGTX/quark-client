package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.*;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BoneMeal2 extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to search for crops/saplings", 3.0, 1.0, 6.0));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between bone meal uses", 200, 50, 2000));

    private final TimerUtil timer = new TimerUtil();

    public BoneMeal2() {
        super("BoneMeal2", "Auto-uses bone meal on crops/saplings", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Find bone meal in hotbar
        int boneMealSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.BONE_MEAL)) {
                boneMealSlot = i;
                break;
            }
        }
        if (boneMealSlot == -1) return;

        int r = (int) Math.ceil(range.get());
        BlockPos center = mc.player.getBlockPos();
        double rangeSq = range.get() * range.get();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > rangeSq) continue;
            Block block = mc.world.getBlockState(pos).getBlock();
            if (!isTarget(block)) continue;

            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = boneMealSlot;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.getInventory().selectedSlot = prev;
            timer.reset();
            return;
        }
    }

    private boolean isTarget(Block block) {
        return block instanceof CropBlock
                || block instanceof SaplingBlock
                || block == Blocks.GRASS_BLOCK
                || block instanceof SugarCaneBlock
                || block == Blocks.NETHER_WART
                || block instanceof MushroomPlantBlock;
    }
}
