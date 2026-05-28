package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoFlatten extends Module {

    private final IntSetting radius = register(new IntSetting("Radius", "Flatten radius", 4, 2, 8));
    private final BoolSetting smooth = register(new BoolSetting("Smooth", "Fill holes below target Y with blocks", false));

    private final TimerUtil timer = new TimerUtil();

    public AutoFlatten() {
        super("AutoFlatten", "Breaks blocks above feet Y and optionally fills holes below", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(50)) return;
        timer.reset();

        int r = radius.get();
        int baseY = mc.player.getBlockPos().getY();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, 1, -r), center.add(r, 4, r))) {
            if (pos.getY() <= baseY) continue;
            if (mc.world.getBlockState(pos).isAir()) continue;
            if (mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK) continue;
            mc.interactionManager.attackBlock(pos.toImmutable(), Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);
            return;
        }

        if (smooth.isEnabled()) {
            for (BlockPos pos : BlockPos.iterate(center.add(-r, baseY - 3, -r), center.add(r, baseY - 1, r))) {
                if (!mc.world.getBlockState(pos).isAir()) continue;
                BlockPos below = pos.down();
                if (mc.world.getBlockState(below).isAir()) continue;
                int slot = findBlockSlot();
                if (slot == -1) break;
                int saved = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = slot;
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(below).add(0, 0.5, 0), Direction.UP, below.toImmutable(), false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.player.getInventory().selectedSlot = saved;
                return;
            }
        }
    }

    private int findBlockSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (!mc.player.getInventory().getStack(i).isEmpty()
                    && mc.player.getInventory().getStack(i).getItem() instanceof BlockItem) return i;
        }
        return -1;
    }
}
