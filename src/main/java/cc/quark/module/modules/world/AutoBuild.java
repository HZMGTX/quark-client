package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class AutoBuild extends Module {

    private final ModeSetting shape = register(new ModeSetting(
            "Shape", "Building shape", "Floor", "Floor", "Wall", "Pillar", "Cube", "Line"));
    private final IntSetting width = register(new IntSetting(
            "Width", "Width of the structure", 3, 1, 16));
    private final IntSetting height = register(new IntSetting(
            "Height", "Height of the structure", 3, 1, 16));
    private final BoolSetting rotate = register(new BoolSetting(
            "Rotate", "Face blocks while placing", true));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Delay between placements (ms)", 50, 0, 200));

    private final TimerUtil timer = new TimerUtil();
    private int buildIndex = 0;
    private List<BlockPos> targetPositions = new ArrayList<>();

    public AutoBuild() {
        super("AutoBuild", "Places blocks in various shapes around you", Category.WORLD);
    }

    @Override
    public void onEnable() {
        buildIndex = 0;
        targetPositions.clear();
        timer.reset();
    }

    @Override
    public String getSuffix() {
        return shape.get();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;
        if (!timer.hasReached(delay.get())) return;

        // Regenerate target positions each tick based on current player position
        targetPositions = generateTargets();

        if (targetPositions.isEmpty()) return;

        // Iterate through targets, find next empty position to fill
        for (BlockPos pos : targetPositions) {
            if (!mc.world.getBlockState(pos).isAir()) continue;

            // Find a solid adjacent block to place against
            BlockPos supportPos = null;
            Direction placeDir = null;

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.offset(dir);
                if (!mc.world.getBlockState(neighbor).isAir()
                        && !mc.world.getBlockState(neighbor).getBlock().getDefaultState().isAir()) {
                    supportPos = neighbor;
                    placeDir = dir.getOpposite();
                    break;
                }
            }

            if (supportPos == null) continue;

            if (rotate.isEnabled()) {
                double dx = pos.getX() + 0.5 - mc.player.getX();
                double dy = pos.getY() + 0.5 - mc.player.getEyeY();
                double dz = pos.getZ() + 0.5 - mc.player.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                mc.player.setYaw((float) Math.toDegrees(Math.atan2(-dx, dz)));
                mc.player.setPitch((float) -Math.toDegrees(Math.atan2(dy, dist)));
            }

            BlockHitResult hit = new BlockHitResult(
                    Vec3d.ofCenter(supportPos), placeDir, supportPos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            timer.reset();
            return;
        }
    }

    private List<BlockPos> generateTargets() {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos playerPos = mc.player.getBlockPos();
        int w = width.get();
        int h = height.get();
        Direction facing = mc.player.getHorizontalFacing();

        switch (shape.get()) {
            case "Floor" -> {
                // Width x Width square one block below player
                BlockPos base = playerPos.down();
                for (int x = -(w / 2); x <= w / 2; x++) {
                    for (int z = -(w / 2); z <= w / 2; z++) {
                        positions.add(base.add(x, 0, z));
                    }
                }
            }
            case "Pillar" -> {
                // Place blocks downward below current position
                for (int i = 1; i <= h; i++) {
                    positions.add(playerPos.down(i));
                }
            }
            case "Wall" -> {
                // Width wide, Height tall wall in front of player
                Direction right = facing.rotateYClockwise();
                BlockPos wallBase = playerPos.offset(facing, 1);
                for (int x = -(w / 2); x <= w / 2; x++) {
                    for (int y = 0; y < h; y++) {
                        positions.add(wallBase.offset(right, x).up(y));
                    }
                }
            }
            case "Cube" -> {
                // Width x Width x Height box around player
                for (int x = -(w / 2); x <= w / 2; x++) {
                    for (int z = -(w / 2); z <= w / 2; z++) {
                        for (int y = 0; y < h; y++) {
                            positions.add(playerPos.add(x, y, z));
                        }
                    }
                }
            }
            case "Line" -> {
                // Place blocks in the direction player is looking, Width blocks long
                for (int i = 1; i <= w; i++) {
                    positions.add(playerPos.offset(facing, i));
                }
            }
        }

        return positions;
    }
}
