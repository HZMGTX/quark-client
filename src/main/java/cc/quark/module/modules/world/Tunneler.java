package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.InventoryUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class Tunneler extends Module {

    private final IntSetting width = register(new IntSetting(
            "Width", "Tunnel width", 1, 1, 3));

    private final IntSetting height = register(new IntSetting(
            "Height", "Tunnel height", 2, 2, 3));

    private final BoolSetting autoMove = register(new BoolSetting(
            "Auto Move", "Automatically walk forward when blocks are broken", true));

    private final BoolSetting torchPlacer = register(new BoolSetting(
            "Torch Placer", "Place torches every 8 blocks", false));

    private int blocksBroken = 0;
    private BlockPos lastBase = null;

    public Tunneler() {
        super("Tunneler", "Automatically mines a tunnel in the look direction", Category.WORLD);
    }

    @Override
    public void onEnable() {
        blocksBroken = 0;
        lastBase = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        int pickSlot = InventoryUtil.findBestPickaxe();
        if (pickSlot == -1) return;
        if (pickSlot < 9) mc.player.getInventory().selectedSlot = pickSlot;

        Direction facing = mc.player.getHorizontalFacing();
        BlockPos base = mc.player.getBlockPos();

        int w = width.get();
        int h = height.get();
        int half = w / 2;

        List<BlockPos> toBreak = new ArrayList<>();

        for (int depth = 0; depth <= 2; depth++) {
            for (int wOffset = -half; wOffset <= half; wOffset++) {
                for (int hOffset = 0; hOffset < h; hOffset++) {
                    BlockPos pos = base.offset(facing, depth + 1).up(hOffset);
                    if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                        pos = pos.east(wOffset);
                    } else {
                        pos = pos.north(wOffset);
                    }
                    if (!mc.world.getBlockState(pos).isAir()
                            && mc.world.getBlockState(pos).getBlock() != Blocks.BEDROCK
                            && mc.world.getBlockState(pos).getHardness(mc.world, pos) >= 0) {
                        toBreak.add(pos);
                    }
                }
            }
        }

        boolean allClear = toBreak.isEmpty();

        for (BlockPos pos : toBreak) {
            Direction face = getBreakFace(pos, facing);
            mc.interactionManager.attackBlock(pos, face);
            blocksBroken++;
        }

        if (torchPlacer.isEnabled() && blocksBroken > 0 && blocksBroken % 8 == 0) {
            placeTorch(base, facing);
        }

        if (autoMove.isEnabled()) {
            if (allClear) {
                mc.player.input.movementForward = 1.0f;
            } else {
                mc.player.input.movementForward = 0.0f;
            }
        }
    }

    private Direction getBreakFace(BlockPos pos, Direction facing) {
        return facing.getOpposite();
    }

    private void placeTorch(BlockPos base, Direction facing) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        int torchSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TORCH) {
                torchSlot = i;
                break;
            }
        }
        if (torchSlot == -1) return;

        Direction side = facing.rotateYClockwise();
        BlockPos wallPos = base.offset(side);
        BlockState wallState = mc.world.getBlockState(wallPos);
        if (wallState.isAir() || !wallState.isSolidBlock(mc.world, wallPos)) {
            side = facing.rotateYCounterclockwise();
            wallPos = base.offset(side);
            wallState = mc.world.getBlockState(wallPos);
        }
        if (!wallState.isAir()) {
            BlockPos torchPos = base.offset(side.getOpposite());
            if (mc.world.getBlockState(torchPos).isAir()) {
                int savedSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = torchSlot;
                Vec3d hitVec = Vec3d.ofCenter(wallPos);
                BlockHitResult hit = new BlockHitResult(hitVec, side.getOpposite(), wallPos, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.getInventory().selectedSlot = savedSlot;
            }
        }
    }

    private BlockState getBlockState(BlockPos pos) {
        if (mc.world == null) return null;
        return mc.world.getBlockState(pos);
    }
}
