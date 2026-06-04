package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

public class MiniMap extends Module {

    private final IntSetting size = register(new IntSetting(
            "Size", "Minimap pixel radius", 80, 32, 160));

    private final IntSetting x = register(new IntSetting(
            "X", "Minimap X position on screen", 10, 0, 3000));

    private final IntSetting y = register(new IntSetting(
            "Y", "Minimap Y position on screen", 10, 0, 3000));

    public MiniMap() {
        super("MiniMap", "Shows a simple minimap on HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        DrawContext ctx = event.getDrawContext();
        int mapX = x.get();
        int mapY = y.get();
        int s = size.get();

        // Background
        ctx.fill(mapX, mapY, mapX + s * 2, mapY + s * 2, 0xAA111111);

        int playerBX = mc.player.getBlockX();
        int playerBZ = mc.player.getBlockZ();
        int playerBY = mc.player.getBlockY();

        float pixelPerBlock = (float) s * 2 / (s * 2);

        // Draw terrain pixels (1 pixel per block)
        for (int dx = -s; dx < s; dx++) {
            for (int dz = -s; dz < s; dz++) {
                BlockPos bp = new BlockPos(playerBX + dx, playerBY, playerBZ + dz);
                // Find topmost solid block
                BlockPos top = mc.world.getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, bp);
                Block block = mc.world.getBlockState(top.down()).getBlock();

                int blockColor = getBlockColor(block);
                int px = mapX + s + dx;
                int py = mapY + s + dz;
                ctx.fill(px, py, px + 1, py + 1, blockColor);
            }
        }

        // Player dot (center)
        ctx.fill(mapX + s - 1, mapY + s - 1, mapX + s + 2, mapY + s + 2, 0xFFFFFFFF);

        // Other players
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            int pdx = (int) (player.getX() - mc.player.getX());
            int pdz = (int) (player.getZ() - mc.player.getZ());
            if (Math.abs(pdx) >= s || Math.abs(pdz) >= s) continue;
            int px = mapX + s + pdx;
            int py = mapY + s + pdz;
            ctx.fill(px - 1, py - 1, px + 2, py + 2, 0xFFFF4444);
        }

        // Border
        ctx.drawBorder(mapX, mapY, s * 2, s * 2, 0xFF00AAFF);
    }

    private int getBlockColor(Block block) {
        if (block == Blocks.GRASS_BLOCK) return 0xFF4CAF50;
        if (block == Blocks.WATER || block == Blocks.BUBBLE_COLUMN) return 0xFF2196F3;
        if (block == Blocks.SAND || block == Blocks.SANDSTONE) return 0xFFF5F5DC;
        if (block == Blocks.STONE || block == Blocks.DEEPSLATE) return 0xFF808080;
        if (block == Blocks.DIRT || block == Blocks.COARSE_DIRT) return 0xFF795548;
        if (block == Blocks.SNOW || block == Blocks.SNOW_BLOCK) return 0xFFF0F0FF;
        if (block == Blocks.LAVA) return 0xFFFF6600;
        if (block == Blocks.OAK_LOG || block == Blocks.BIRCH_LOG || block == Blocks.SPRUCE_LOG) return 0xFF6D4C41;
        if (block == Blocks.OAK_LEAVES || block == Blocks.BIRCH_LEAVES || block == Blocks.SPRUCE_LEAVES) return 0xFF2E7D32;
        if (block == Blocks.AIR || block == Blocks.VOID_AIR) return 0xFF111111;
        return 0xFF556B2F; // default greenish
    }
}
