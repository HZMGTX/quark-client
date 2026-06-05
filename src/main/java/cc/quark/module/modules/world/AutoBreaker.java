package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoBreaker extends Module {
    private final DoubleSetting range = register(new DoubleSetting("Range", "Break radius", 3.0, 1.0, 6.0));
    private final ModeSetting target = register(new ModeSetting("Target", "Blocks to break", "Ore", "Ore", "Crops", "All", "Custom"));
    private final IntSetting delay = register(new IntSetting("Delay", "Ticks between breaks", 1, 0, 20));
    private final BoolSetting requireTool = register(new BoolSetting("Best Tool", "Auto-switch to best tool", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoBreaker() {
        super("Auto Breaker", "Automatically breaks blocks in range", Category.WORLD, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get() * 50L)) return;
        timer.reset();

        BlockPos playerPos = mc.player.getBlockPos();
        int r = (int) Math.ceil(range.get());

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (pos.getSquaredDistance(playerPos.getX(), playerPos.getY(), playerPos.getZ()) > range.get() * range.get()) continue;

                    var state = mc.world.getBlockState(pos);
                    Block block = state.getBlock();

                    boolean shouldBreak = false;
                    if (target.get().equals("All") && block != Blocks.AIR && block != Blocks.BEDROCK) {
                        shouldBreak = true;
                    } else if (target.get().equals("Ore")) {
                        shouldBreak = isOre(block);
                    } else if (target.get().equals("Crops")) {
                        shouldBreak = isCrop(block);
                    }

                    if (shouldBreak) {
                        mc.interactionManager.attackBlock(pos, Direction.UP);
                        return;
                    }
                }
            }
        }
    }

    private boolean isOre(Block b) {
        return b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE ||
               b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE ||
               b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE ||
               b == Blocks.EMERALD_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE ||
               b == Blocks.COAL_ORE || b == Blocks.DEEPSLATE_COAL_ORE ||
               b == Blocks.ANCIENT_DEBRIS || b == Blocks.NETHER_GOLD_ORE;
    }

    private boolean isCrop(Block b) {
        return b == Blocks.WHEAT || b == Blocks.CARROTS || b == Blocks.POTATOES ||
               b == Blocks.BEETROOTS || b == Blocks.NETHER_WART || b == Blocks.MELON;
    }
}
