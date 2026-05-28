package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoBridge2 extends Module {

    private final DoubleSetting delay = register(new DoubleSetting("Delay", "Milliseconds between placements", 50.0, 0.0, 500.0));

    private final TimerUtil timer = new TimerUtil();

    public AutoBridge2() {
        super("AutoBridge2", "Bridges blocks backwards automatically when walking backward over an edge", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        if (mc.player.input.movementForward >= 0) return;

        int blockSlot = findBlockSlot();
        if (blockSlot == -1) return;

        BlockPos feet = mc.player.getBlockPos();
        BlockPos below = feet.down();

        if (!mc.world.getBlockState(below).isAir()) return;

        Direction facing = mc.player.getHorizontalFacing();
        Direction placeDir = facing.getOpposite();

        BlockPos support = below.offset(placeDir.getOpposite());
        if (mc.world.getBlockState(support).isAir()) return;

        int saved = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        Vec3d hitVec = Vec3d.ofCenter(support).add(Vec3d.of(placeDir.getVector()).multiply(0.5));
        BlockHitResult hit = new BlockHitResult(hitVec, placeDir, support.toImmutable(), false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);

        mc.player.getInventory().selectedSlot = saved;
        timer.reset();
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
