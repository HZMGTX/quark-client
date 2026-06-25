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

public class CropBooster extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to scan for immature crops to boost", 4.0, 1.0, 8.0));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between bone meal applications", 100, 50, 1000));

    private final TimerUtil timer = new TimerUtil();

    public CropBooster() {
        super("CropBooster", "Speeds up crop growth by auto-using bone meal on immature plants", Category.WORLD);
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
        double rangeSq = range.get() * range.get();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > rangeSq) continue;
            var state = mc.world.getBlockState(pos);
            Block block = state.getBlock();

            // Only boost immature crops
            boolean isImmatureCrop = (block instanceof CropBlock crop && !crop.isMature(state))
                    || (block instanceof SaplingBlock)
                    || (block == Blocks.NETHER_WART && state.contains(net.minecraft.state.property.Properties.AGE_3)
                        && state.get(net.minecraft.state.property.Properties.AGE_3) < 3);

            if (!isImmatureCrop) continue;

            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = boneMealSlot;
            Vec3d hitVec = Vec3d.ofCenter(pos);
            BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = prev;
            timer.reset();
            return;
        }
        timer.reset();
    }
}
