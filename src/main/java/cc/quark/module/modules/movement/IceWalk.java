package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/**
 * IceWalk - lets the player walk on water and/or lava surfaces as if they
 * were solid ice by replacing liquid blocks underfoot with packed ice each
 * tick and restoring them when the player steps away.
 *
 * <p>Unlike FrostWalk (which uses frosted ice and lets it melt naturally),
 * IceWalk keeps the ice alive while the module is enabled, providing a
 * persistent dry surface.  Sneaking lets the player sink into the liquid.
 */
public class IceWalk extends Module {

    private final BoolSetting water = register(new BoolSetting(
            "Water", "Walk on water surfaces", true));

    private final BoolSetting lava = register(new BoolSetting(
            "Lava", "Walk on lava surfaces", false));

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Block radius to solidify around the player's feet", 1, 1, 3));

    private final BoolSetting restore = register(new BoolSetting(
            "Restore", "Remove placed ice blocks when stepping away", true));

    public IceWalk() {
        super("IceWalk", "Walk on water/lava by placing packed-ice blocks underfoot", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isSneaking()) return;

        int r = radius.get();
        BlockPos feet = mc.player.getBlockPos();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > r * r + r) continue; // rough circle

                BlockPos pos = feet.add(dx, 0, dz);
                BlockState state = mc.world.getBlockState(pos);

                boolean isWaterSurface = water.isEnabled()
                        && state.isOf(Blocks.WATER)
                        && state.getFluidState().isStill();

                boolean isLavaSurface = lava.isEnabled()
                        && state.isOf(Blocks.LAVA)
                        && state.getFluidState().isStill();

                if (isWaterSurface || isLavaSurface) {
                    mc.world.setBlockState(pos, Blocks.PACKED_ICE.getDefaultState());
                }
            }
        }

        // Restore: re-place water/lava a radius+1 ring out when restore is on
        if (restore.isEnabled()) {
            int outerR = r + 2;
            for (int dx = -outerR; dx <= outerR; dx++) {
                for (int dz = -outerR; dz <= outerR; dz++) {
                    int distSq = dx * dx + dz * dz;
                    if (distSq <= r * r + r) continue;       // skip inner (still in use)
                    if (distSq > outerR * outerR) continue;  // skip outer

                    BlockPos pos = feet.add(dx, 0, dz);
                    BlockState state = mc.world.getBlockState(pos);
                    if (state.isOf(Blocks.PACKED_ICE)) {
                        // Restore to water; server will correct lava-sourced blocks
                        mc.world.setBlockState(pos, Blocks.WATER.getDefaultState());
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        // Nothing client-side to forcibly clean up;
        // the server will correct any mismatched blocks on the next tick.
    }
}
