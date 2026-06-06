package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * HoleSnap - instantly teleports/snaps the player into the nearest safe hole
 * (1x1 pit surrounded by solid blocks) when triggered.
 *
 * Safe holes are defined as positions where:
 *  - The block at and above are air
 *  - All 4 cardinal neighbors at foot-level are solid/unbreakable blocks
 */
public class HoleSnap extends Module {

    private final DoubleSetting searchRadius = register(new DoubleSetting(
            "Radius", "Block radius to search for holes", 5.0, 1.0, 10.0));

    private final BoolSetting onlyObsidian = register(new BoolSetting(
            "Only Obsidian", "Only count holes surrounded by obsidian/bedrock", true));

    private final BoolSetting snapOnEnable = register(new BoolSetting(
            "Snap On Enable", "Snap to hole immediately when module is enabled", true));

    private boolean snapped = false;

    public HoleSnap() {
        super("HoleSnap", "Snaps player into the nearest safe hole with one toggle", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
        snapped = false;
        if (snapOnEnable.isEnabled()) {
            doSnap();
        }
    }

    @Override
    public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        // Disable self after snapping so it acts as a one-shot trigger
        if (snapOnEnable.isEnabled() && !snapped) {
            // Already handled in onEnable; auto-disable
            this.disable();
        }
    }

    private void doSnap() {
        if (mc.player == null || mc.world == null) return;

        BlockPos best = findNearestHole();
        if (best == null) return;

        // Snap to center of the hole
        double snapX = best.getX() + 0.5;
        double snapY = best.getY();
        double snapZ = best.getZ() + 0.5;

        mc.player.setPosition(snapX, snapY, snapZ);
        mc.player.setVelocity(Vec3d.ZERO);
        snapped = true;
    }

    private BlockPos findNearestHole() {
        BlockPos playerPos = mc.player.getBlockPos();
        int r = (int) Math.ceil(searchRadius.get());
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -2; dy <= 0; dy++) {
                    BlockPos candidate = playerPos.add(dx, dy, dz);
                    if (!isSafeHole(candidate)) continue;
                    double dist = candidate.getSquaredDistance(playerPos);
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = candidate;
                    }
                }
            }
        }
        return best;
    }

    private boolean isSafeHole(BlockPos pos) {
        if (mc.world == null) return false;

        // Foot and head positions must be air
        if (!mc.world.getBlockState(pos).isAir()) return false;
        if (!mc.world.getBlockState(pos.up()).isAir()) return false;

        // Floor must be solid
        BlockState floor = mc.world.getBlockState(pos.down());
        if (floor.isAir()) return false;

        // Check 4 cardinal neighbors
        BlockPos[] neighbors = {
            pos.north(), pos.south(), pos.east(), pos.west()
        };

        for (BlockPos n : neighbors) {
            BlockState state = mc.world.getBlockState(n);
            if (state.isAir()) return false;
            if (onlyObsidian.isEnabled()) {
                net.minecraft.block.Block block = state.getBlock();
                if (block != Blocks.OBSIDIAN && block != Blocks.CRYING_OBSIDIAN
                        && block != Blocks.BEDROCK && block != Blocks.REINFORCED_DEEPSLATE) {
                    return false;
                }
            }
        }
        return true;
    }
}
