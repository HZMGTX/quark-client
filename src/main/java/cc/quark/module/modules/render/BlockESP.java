package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class BlockESP extends Module {

    private final ModeSetting blockMode = register(new ModeSetting(
            "Block", "Which block type to highlight",
            "Diamond Ore",
            "Diamond Ore", "Ancient Debris", "Chest", "Spawner", "All Ores"));
    private final IntSetting range = register(new IntSetting(
            "Range", "Search radius in blocks", 20, 10, 50));
    private final ColorSetting color = register(new ColorSetting(
            "Color", "ESP color", 0xFF00FFFF));
    private final IntSetting scanInterval = register(new IntSetting(
            "Scan Interval", "Ticks between rescans", 20, 5, 100));

    private final List<BlockPos> found = new ArrayList<>();
    private int scanTimer = 0;

    public BlockESP() {
        super("BlockESP", "Highlights specific block types in the world", Category.RENDER);
    }

    @Override
    public void onEnable() {
        found.clear();
        scanTimer = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        scanTimer--;
        if (scanTimer > 0) return;
        scanTimer = scanInterval.get();
        scan();
    }

    private void scan() {
        found.clear();
        BlockPos center = mc.player.getBlockPos();
        int r = range.get();
        for (BlockPos pos : BlockPos.iterate(
                center.add(-r, -r, -r), center.add(r, r, r))) {
            BlockState state = mc.world.getBlockState(pos);
            if (matches(state.getBlock())) {
                found.add(pos.toImmutable());
            }
        }
    }

    private boolean matches(Block block) {
        return switch (blockMode.get()) {
            case "Diamond Ore" -> block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE;
            case "Ancient Debris" -> block == Blocks.ANCIENT_DEBRIS;
            case "Chest" -> block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.BARREL || block == Blocks.ENDER_CHEST;
            case "Spawner" -> block == Blocks.SPAWNER;
            case "All Ores" -> isOre(block);
            default -> false;
        };
    }

    private boolean isOre(Block block) {
        return block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE
                || block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE
                || block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE
                || block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE
                || block == Blocks.ANCIENT_DEBRIS
                || block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE
                || block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE
                || block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE
                || block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE
                || block == Blocks.NETHER_QUARTZ_ORE;
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();
        for (BlockPos pos : found) {
            Box box = new Box(pos);
            RenderUtil.drawESPBox(event.getMatrixStack(), box, r, g, b, 0.9f, 1.5f);
            RenderUtil.drawFilledBox(event.getMatrixStack(), box, r, g, b, 0.15f);
        }
    }

    @Override
    public String getSuffix() {
        return found.size() + " blocks";
    }
}
