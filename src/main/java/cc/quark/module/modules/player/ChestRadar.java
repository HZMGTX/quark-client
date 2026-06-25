package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ChestRadar extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to scan for chests", 16.0, 4.0, 64.0));

    private final BoolSetting miniMap = register(new BoolSetting(
            "Mini Map", "Show chests on mini-map overlay", true));

    public ChestRadar() {
        super("ChestRadar", "Shows nearby chests on compass", Category.PLAYER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        if (!miniMap.isEnabled()) return;

        int r = (int) range.get();
        BlockPos origin = mc.player.getBlockPos();
        List<BlockPos> chests = new ArrayList<>();

        for (int x = -r; x <= r; x += 2) {
            for (int y = -r; y <= r; y += 2) {
                for (int z = -r; z <= r; z += 2) {
                    BlockPos pos = origin.add(x, y, z);
                    var block = mc.world.getBlockState(pos).getBlock();
                    if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST
                            || block == Blocks.ENDER_CHEST || block == Blocks.BARREL) {
                        chests.add(pos);
                    }
                }
            }
        }

        // Draw mini-map dots in top-right corner
        int mapX = sw - 70;
        int mapY = 10;
        int mapSize = 60;
        ctx.fill(mapX, mapY, mapX + mapSize, mapY + mapSize, 0x88000000);
        ctx.drawBorder(mapX, mapY, mapSize, mapSize, 0xFFFFFFFF);

        for (BlockPos chest : chests) {
            double dx = chest.getX() - origin.getX();
            double dz = chest.getZ() - origin.getZ();
            int px = mapX + mapSize / 2 + (int)(dx * mapSize / (r * 2.0));
            int py = mapY + mapSize / 2 + (int)(dz * mapSize / (r * 2.0));
            if (px >= mapX && px <= mapX + mapSize && py >= mapY && py <= mapY + mapSize) {
                ctx.fill(px - 1, py - 1, px + 1, py + 1, 0xFFFFAA00);
            }
        }

        ctx.fill(mapX + mapSize / 2 - 1, mapY + mapSize / 2 - 1,
                mapX + mapSize / 2 + 1, mapY + mapSize / 2 + 1, 0xFF00FF00);

        ctx.drawTextWithShadow(mc.textRenderer, "Chests: " + chests.size(),
                mapX, mapY + mapSize + 2, 0xFFFFFFFF);
    }
}
