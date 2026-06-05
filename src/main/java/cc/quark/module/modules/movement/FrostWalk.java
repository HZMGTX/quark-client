package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

/**
 * FrostWalk - freezes water blocks in a radius around the player's feet so they
 * can walk on water without an enchantment.
 *
 * <p>Each tick, checks all blocks within the configured radius at the player's
 * foot Y level.  Any water surface block is replaced with frosted ice, exactly
 * mimicking the Frost Walker enchantment effect.
 */
public class FrostWalk extends Module {

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Block radius to freeze water around feet", 2, 1, 4));

    public FrostWalk() {
        super("FrostWalk", "Freeze water blocks underfoot like the Frost Walker enchantment", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround() && !mc.player.isTouchingWater()) return;

        int r       = radius.get();
        BlockPos feet = mc.player.getBlockPos();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > r * r) continue; // circle

                BlockPos checkPos = feet.add(dx, 0, dz);
                BlockState state  = mc.world.getBlockState(checkPos);

                // Replace water surface blocks with frosted ice
                if (state.isOf(Blocks.WATER)) {
                    // Check it's a full water source block at the top surface
                    if (state.getFluidState().isStill()) {
                        mc.world.setBlockState(checkPos, Blocks.FROSTED_ICE.getDefaultState());
                    }
                }
            }
        }
    }
}
