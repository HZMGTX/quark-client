package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Scaffolding extends Module {

    private final BoolSetting sprint = register(new BoolSetting(
            "Sprint", "Maintain sprint while scaffolding", true));
    private final BoolSetting tower = register(new BoolSetting(
            "Tower", "Place blocks upward when jump is held", true));

    private final TimerUtil timer = new TimerUtil();

    public Scaffolding() {
        super("Scaffolding", "Auto-places blocks under feet when walking off edges", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(50)) return;

        if (sprint.isEnabled()) mc.player.setSprinting(true);

        int blockSlot = findBlockSlot();
        if (blockSlot == -1) return;

        // Tower mode: place block above current position when jumping
        if (tower.isEnabled() && mc.options.jumpKey.isPressed()) {
            handleTower(blockSlot);
            return;
        }

        // Scaffold mode: place block below when there's air underfoot
        BlockPos feet = mc.player.getBlockPos();
        BlockPos below = feet.down();
        if (!mc.world.getBlockState(below).isAir()) return;

        // Find a support block adjacent to below
        for (Direction dir : Direction.Type.HORIZONTAL) {
            BlockPos support = below.offset(dir);
            if (!mc.world.getBlockState(support).isAir()) {
                placeBlock(blockSlot, support, dir.getOpposite());
                timer.reset();
                return;
            }
        }

        // Try placing on the block two below
        BlockPos twoBelow = below.down();
        if (!mc.world.getBlockState(twoBelow).isAir()) {
            placeBlock(blockSlot, twoBelow, Direction.UP);
            timer.reset();
        }
    }

    private void handleTower(int blockSlot) {
        BlockPos feet = mc.player.getBlockPos();
        BlockPos below = feet.down();
        if (mc.world.getBlockState(below).isAir()) return;

        BlockPos above = feet.up();
        if (!mc.world.getBlockState(above).isAir()) return;

        // Place on top of the block below feet
        placeBlock(blockSlot, below, Direction.UP);
        timer.reset();
    }

    private void placeBlock(int slot, BlockPos target, Direction face) {
        int saved = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;

        Vec3d hitVec = Vec3d.ofCenter(target).add(Vec3d.of(face.getVector()).multiply(0.5));
        BlockHitResult hit = new BlockHitResult(hitVec, face, target, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);

        mc.player.getInventory().selectedSlot = saved;
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
