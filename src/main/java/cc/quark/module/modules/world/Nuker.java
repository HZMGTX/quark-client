package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Nuker extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Block break radius", 4.0, 1.0, 6.0));

    private final IntSetting speed = register(new IntSetting(
            "Speed", "Blocks to break per tick", 1, 1, 5));

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Block selection mode", "Sphere", "Sphere", "Flatten", "Box"));

    private final BoolSetting whitelist = register(new BoolSetting(
            "Whitelist", "Only break specific block types", false));

    private final BoolSetting checkReach = register(new BoolSetting(
            "Check Reach", "Only break blocks with line of sight", false));

    private final Set<BlockPos> breaking = new HashSet<>();

    public Nuker() {
        super("Nuker", "Breaks all nearby blocks automatically", Category.WORLD);
    }

    @Override
    public void onDisable() {
        breaking.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        BlockPos center = mc.player.getBlockPos();
        double r = range.get();
        int ri = (int) Math.ceil(r);
        List<BlockPos> targets = new ArrayList<>();

        Vec3d playerEyes = mc.player.getEyePos();
        double playerY = mc.player.getY();

        for (int x = -ri; x <= ri; x++) {
            for (int y = -ri; y <= ri; y++) {
                for (int z = -ri; z <= ri; z++) {
                    BlockPos pos = center.add(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);

                    if (state.isAir()) continue;
                    if (state.getBlock() == Blocks.BEDROCK) continue;
                    if (state.getHardness(mc.world, pos) < 0) continue;

                    double dist = pos.getSquaredDistance(center);
                    if (dist > r * r) continue;

                    if (mode.is("Flatten") && pos.getY() >= (int) Math.floor(playerY)) continue;

                    if (whitelist.isEnabled() && !isWhitelisted(state.getBlock())) continue;

                    if (checkReach.isEnabled()) {
                        Vec3d blockCenter = Vec3d.ofCenter(pos);
                        if (!mc.world.isAir(pos) && !hasLineOfSight(playerEyes, blockCenter)) continue;
                    }

                    if (!breaking.contains(pos)) {
                        targets.add(pos);
                    }
                }
            }
        }

        targets.sort((a, b) -> Double.compare(
                a.getSquaredDistance(center),
                b.getSquaredDistance(center)));

        int broken = 0;
        for (BlockPos pos : targets) {
            if (broken >= speed.get()) break;

            BlockState state = mc.world.getBlockState(pos);
            if (state.isAir()) continue;

            switchToBestTool(state);

            Direction dir = getBestFace(pos, playerEyes);
            mc.interactionManager.attackBlock(pos, dir);
            breaking.add(pos);
            broken++;
        }

        breaking.removeIf(pos -> mc.world.getBlockState(pos).isAir());
    }

    private Direction getBestFace(BlockPos pos, Vec3d playerEyes) {
        Vec3d blockCenter = Vec3d.ofCenter(pos);
        Vec3d diff = playerEyes.subtract(blockCenter);

        Direction best = Direction.UP;
        double bestDot = Double.NEGATIVE_INFINITY;
        for (Direction dir : Direction.values()) {
            double dot = dir.getUnitVector().getX() * diff.x
                    + dir.getUnitVector().getY() * diff.y
                    + dir.getUnitVector().getZ() * diff.z;
            if (dot > bestDot) {
                bestDot = dot;
                best = dir;
            }
        }
        return best;
    }

    private void switchToBestTool(BlockState state) {
        if (mc.player == null) return;
        int bestSlot = -1;
        float bestSpeed = -1f;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        if (bestSlot != -1) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }

    private boolean hasLineOfSight(Vec3d from, Vec3d to) {
        if (mc.world == null) return false;
        var result = mc.world.raycast(new net.minecraft.world.RaycastContext(
                from, to,
                net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                net.minecraft.world.RaycastContext.FluidHandling.NONE,
                mc.player));
        return result.getType() == net.minecraft.util.hit.HitResult.Type.MISS
                || result.getPos().squaredDistanceTo(to) < 1.0;
    }

    private boolean isWhitelisted(Block block) {
        return block == Blocks.STONE || block == Blocks.COBBLESTONE
                || block == Blocks.DIRT || block == Blocks.GRASS_BLOCK
                || block == Blocks.SAND || block == Blocks.GRAVEL
                || block == Blocks.OAK_LOG || block == Blocks.SPRUCE_LOG
                || block == Blocks.BIRCH_LOG || block == Blocks.JUNGLE_LOG
                || block == Blocks.ACACIA_LOG || block == Blocks.DARK_OAK_LOG;
    }
}
