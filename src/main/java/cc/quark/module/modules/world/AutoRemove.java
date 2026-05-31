package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoRemove extends Module {

    private final ModeSetting blockType = register(new ModeSetting(
            "BlockType", "Which block type to remove",
            "Gravel", "Gravel", "Sand", "Snow", "Lava", "Water", "Vines"));

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Radius to search for blocks to remove", 5, 1, 12));

    private final TimerUtil timer = new TimerUtil();

    public AutoRemove() {
        super("AutoRemove", "Removes specified block types from the world around you", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(100)) return;
        timer.reset();

        Block target = getTargetBlock();
        if (target == null) return;

        BlockPos center = mc.player.getBlockPos();
        int r = radius.get();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            if (mc.world.getBlockState(pos).getBlock() != target) continue;
            mc.interactionManager.attackBlock(pos, Direction.UP);
            mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
            return;
        }
    }

    private Block getTargetBlock() {
        return switch (blockType.get()) {
            case "Gravel" -> Blocks.GRAVEL;
            case "Sand"   -> Blocks.SAND;
            case "Snow"   -> Blocks.SNOW;
            case "Lava"   -> Blocks.LAVA;
            case "Water"  -> Blocks.WATER;
            case "Vines"  -> Blocks.VINE;
            default       -> null;
        };
    }
}
