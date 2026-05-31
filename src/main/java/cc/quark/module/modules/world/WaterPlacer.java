package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class WaterPlacer extends Module {

    private final BoolSetting removeAfter = register(new BoolSetting("RemoveAfter", "Pick water back up with empty bucket after placing", false));

    private final TimerUtil timer = new TimerUtil();
    private BlockPos placedPos = null;

    public WaterPlacer() {
        super("WaterPlacer", "Places water bucket at target position; removes when done", Category.WORLD);
    }

    @Override
    public void onEnable() {
        placedPos = null;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(300)) return;

        if (removeAfter.isEnabled() && placedPos != null) {
            int bucketSlot = findItem(Items.BUCKET);
            if (bucketSlot != -1) {
                int saved = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = bucketSlot;
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(placedPos), Direction.UP, placedPos, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.getInventory().selectedSlot = saved;
                placedPos = null;
                timer.reset();
            }
            return;
        }

        HitResult crosshair = mc.crosshairTarget;
        if (crosshair == null || crosshair.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult bhr = (BlockHitResult) crosshair;
        BlockPos targetPos = bhr.getBlockPos().offset(bhr.getSide());

        int waterBucketSlot = findItem(Items.WATER_BUCKET);
        if (waterBucketSlot == -1) return;

        int saved = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = waterBucketSlot;
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(bhr.getBlockPos()), bhr.getSide(), bhr.getBlockPos(), false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = saved;
        placedPos = targetPos;
        timer.reset();
    }

    private int findItem(net.minecraft.item.Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }
}
