package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Nuker extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Block break radius", 3, 1, 5));

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Block selection mode", "Normal", "Normal", "ID"));

    private String targetId = "minecraft:stone";

    public Nuker() {
        super("Nuker", "Breaks all nearby blocks automatically", Category.WORLD);
    }

    public void setTargetId(String id) {
        this.targetId = id;
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
                    BlockPos pos = center.add(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);

                    if (state.isAir()) continue;
                    if (state.getBlock() == Blocks.BEDROCK) continue;

                    if (mode.is("ID")) {
                        String id = net.minecraft.registry.Registries.BLOCK.getId(state.getBlock()).toString();
                        if (!id.equals(targetId)) continue;
                    }

                    targets.add(pos);
                }
            }
        }

        targets.sort(Comparator.comparingDouble(pos ->
                pos.getSquaredDistance(new Vec3i(center.getX(), center.getY(), center.getZ()))));

        if (!targets.isEmpty()) {
            BlockPos pos = targets.get(0);
            mc.interactionManager.attackBlock(pos, Direction.UP);
        }
    }
}
