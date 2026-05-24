package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.EnumSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Nuker - breaks blocks in a configurable radius around the player every tick.
 *
 * Modes:
 *   All   â€“ break every non-air block in range
 *   Flat  â€“ only break blocks at or below player's foot level
 *   ID    â€“ only break a specific block type (set blockID)
 *
 * Shapes:
 *   Sphere â€“ only blocks within the radius (Euclidean distance)
 *   Cube   â€“ all blocks in the bounding cube
 */
public class Nuker extends Module {

    public enum NukerMode  { ALL, FLAT, ID }
    public enum NukerShape { SPHERE, CUBE }

    private final IntSetting range = register(new IntSetting(
            "Range", "Block break radius", 3, 1, 5));

    private final EnumSetting<NukerMode> mode = register(new EnumSetting<>(
            "Mode", "Which blocks to break", NukerMode.ALL));

    private final EnumSetting<NukerShape> shape = register(new EnumSetting<>(
            "Shape", "Area shape", NukerShape.SPHERE));

    private final IntSetting speed = register(new IntSetting(
            "Speed", "Blocks broken per tick", 1, 1, 20));

    // blockID is entered via command; we store it as a string tag
    private String targetBlockId = "minecraft:stone";

    public Nuker() {
        super("Nuker", "Breaks blocks in range around the player", Category.WORLD);
    }

    public void setTargetBlock(String id) {
        this.targetBlockId = id;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        BlockPos center = mc.player.getBlockPos();
        int r = range.get();

        List<BlockPos> targets = new ArrayList<>();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    if (shape.get() == NukerShape.SPHERE) {
                        if (x * x + y * y + z * z > r * r) continue;
                    }

                    BlockPos pos = center.add(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);

                    if (state.isAir()) continue;
                    if (state.getBlock() == Blocks.BEDROCK) continue;

                    switch (mode.get()) {
                        case FLAT -> {
                            if (pos.getY() > center.getY()) continue;
                        }
                        case ID -> {
                            String id = net.minecraft.registry.Registries.BLOCK.getId(state.getBlock()).toString();
                            if (!id.equals(targetBlockId)) continue;
                        }
                        default -> {} // ALL: no extra filtering
                    }

                    targets.add(pos);
                }
            }
        }

        // Sort by distance to player
        targets.sort(Comparator.comparingDouble(pos ->
                pos.getSquaredDistance(new Vec3i(center.getX(), center.getY(), center.getZ()))));

        int broken = 0;
        for (BlockPos pos : targets) {
            if (broken >= speed.get()) break;

            // Send start-break and stop-break packets to instantly break the block
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.DOWN));
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN));

            // Also break client-side for visual consistency
            mc.world.breakBlock(pos, true, mc.player);

            broken++;
        }
    }
}
