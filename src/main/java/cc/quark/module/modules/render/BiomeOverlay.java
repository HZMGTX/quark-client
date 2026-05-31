package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.HashMap;
import java.util.Map;

public class BiomeOverlay extends Module {

    // Biome key → packed ARGB (alpha intentionally low for overlay)
    private static final Map<RegistryKey<Biome>, int[]> BIOME_COLORS = new HashMap<>();

    static {
        // r,g,b as 0-255
        BIOME_COLORS.put(BiomeKeys.OCEAN,              new int[]{0,   80,  200});
        BIOME_COLORS.put(BiomeKeys.DEEP_OCEAN,         new int[]{0,   40,  160});
        BIOME_COLORS.put(BiomeKeys.PLAINS,             new int[]{100, 200, 60});
        BIOME_COLORS.put(BiomeKeys.FOREST,             new int[]{30,  130, 30});
        BIOME_COLORS.put(BiomeKeys.DARK_FOREST,        new int[]{20,  60,  20});
        BIOME_COLORS.put(BiomeKeys.JUNGLE,             new int[]{0,   180, 0});
        BIOME_COLORS.put(BiomeKeys.DESERT,             new int[]{230, 210, 100});
        BIOME_COLORS.put(BiomeKeys.SAVANNA,            new int[]{200, 180, 60});
        BIOME_COLORS.put(BiomeKeys.BADLANDS,           new int[]{200, 100, 30});
        BIOME_COLORS.put(BiomeKeys.TAIGA,              new int[]{80,  130, 80});
        BIOME_COLORS.put(BiomeKeys.SNOWY_PLAINS,       new int[]{220, 240, 255});
        BIOME_COLORS.put(BiomeKeys.SWAMP,              new int[]{60,  100, 40});
        BIOME_COLORS.put(BiomeKeys.RIVER,              new int[]{50,  120, 200});
        BIOME_COLORS.put(BiomeKeys.MUSHROOM_FIELDS,    new int[]{200, 50,  200});
        BIOME_COLORS.put(BiomeKeys.NETHER_WASTES,      new int[]{180, 30,  10});
        BIOME_COLORS.put(BiomeKeys.THE_END,            new int[]{100, 80,  150});
        BIOME_COLORS.put(BiomeKeys.BEACH,              new int[]{220, 200, 120});
        BIOME_COLORS.put(BiomeKeys.BIRCH_FOREST,       new int[]{160, 200, 140});
        BIOME_COLORS.put(BiomeKeys.MEADOW,             new int[]{140, 220, 80});
        BIOME_COLORS.put(BiomeKeys.FROZEN_RIVER,       new int[]{160, 200, 240});
        BIOME_COLORS.put(BiomeKeys.SNOWY_BEACH,        new int[]{200, 220, 240});
        BIOME_COLORS.put(BiomeKeys.STONY_PEAKS,        new int[]{150, 150, 150});
        BIOME_COLORS.put(BiomeKeys.WINDSWEPT_HILLS,    new int[]{100, 120, 80});
        BIOME_COLORS.put(BiomeKeys.CAVE,               new int[]{60,  60,  60});
    }

    public BiomeOverlay() {
        super("BiomeOverlay", "Colors chunks by biome type", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        BlockPos playerPos = mc.player.getBlockPos();
        ChunkPos playerChunk = new ChunkPos(playerPos);
        int radius = 4;

        for (int cx = playerChunk.x - radius; cx <= playerChunk.x + radius; cx++) {
            for (int cz = playerChunk.z - radius; cz <= playerChunk.z + radius; cz++) {
                BlockPos center = new BlockPos(cx * 16 + 8, playerPos.getY(), cz * 16 + 8);
                RegistryEntry<Biome> biomeEntry = mc.world.getBiome(center);

                int[] rgb = biomeEntry.getKey().map(BIOME_COLORS::get).orElse(null);
                float r, g, b;
                if (rgb != null) {
                    r = rgb[0] / 255f; g = rgb[1] / 255f; b = rgb[2] / 255f;
                } else {
                    r = 0.5f; g = 0.5f; b = 0.5f;
                }

                Box box = new Box(cx * 16, playerPos.getY() - 1, cz * 16, cx * 16 + 16, playerPos.getY(), cz * 16 + 16);
                RenderUtil.drawFilledBox(m, box, r, g, b, 0.20f);
                RenderUtil.drawESPBox(m, box, r, g, b, 0.6f, 1.0f);
            }
        }
    }
}
