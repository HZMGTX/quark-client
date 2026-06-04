package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorldScanner extends Module {

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Chunk scan radius", 128, 16, 512));
    private final StringSetting target = register(new StringSetting(
            "Target", "Structure type to find (village, stronghold, dungeon)", "village"));

    private final TimerUtil scanTimer = new TimerUtil();
    private final Set<ChunkPos> scannedChunks = new HashSet<>();
    private final Map<ChunkPos, String> foundStructures = new HashMap<>();

    public WorldScanner() {
        super("WorldScanner", "Scans loaded chunks for specific structure patterns", Category.WORLD);
    }

    @Override
    public void onEnable() {
        scannedChunks.clear();
        foundStructures.clear();
        scanTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!scanTimer.hasReached(2000)) return;
        scanTimer.reset();

        int r = radius.get();
        int chunkR = r >> 4;
        ChunkPos playerChunk = new ChunkPos(mc.player.getBlockPos());

        for (int cx = playerChunk.x - chunkR; cx <= playerChunk.x + chunkR; cx++) {
            for (int cz = playerChunk.z - chunkR; cz <= playerChunk.z + chunkR; cz++) {
                ChunkPos cp = new ChunkPos(cx, cz);
                if (scannedChunks.contains(cp)) continue;
                if (mc.world.getChunk(cx, cz) == null) continue;
                scannedChunks.add(cp);

                String t = target.get().toLowerCase().trim();
                boolean found = switch (t) {
                    case "village"    -> hasVillageBlocks(cp);
                    case "stronghold" -> hasStrongholdBlocks(cp);
                    case "dungeon"    -> hasDungeonBlocks(cp);
                    default           -> false;
                };

                if (found && !foundStructures.containsKey(cp)) {
                    foundStructures.put(cp, t);
                    int wx = cx * 16 + 8;
                    int wz = cz * 16 + 8;
                    ChatUtil.info("[WorldScanner] Found " + t + " near chunk " + cx + "," + cz
                            + " (approx " + wx + " ? " + wz + ")");
                }
            }
        }
    }

    private boolean hasVillageBlocks(ChunkPos cp) {
        if (mc.world == null) return false;
        int count = 0;
        for (int x = cp.getStartX(); x <= cp.getEndX(); x += 2) {
            for (int z = cp.getStartZ(); z <= cp.getEndZ(); z += 2) {
                for (int y = mc.world.getBottomY(); y < mc.world.getTopY(); y += 2) {
                    Block b = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (b == Blocks.HAY_BLOCK || b == Blocks.BELL || b == Blocks.CARVED_PUMPKIN
                            || b == Blocks.COMPOSTER || b == Blocks.LECTERN) {
                        count++;
                        if (count >= 3) return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasStrongholdBlocks(ChunkPos cp) {
        if (mc.world == null) return false;
        int count = 0;
        for (int x = cp.getStartX(); x <= cp.getEndX(); x += 2) {
            for (int z = cp.getStartZ(); z <= cp.getEndZ(); z += 2) {
                for (int y = mc.world.getBottomY(); y < 64; y += 2) {
                    Block b = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (b == Blocks.END_PORTAL_FRAME || b == Blocks.INFESTED_STONE
                            || b == Blocks.INFESTED_STONE_BRICKS) {
                        count++;
                        if (count >= 2) return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasDungeonBlocks(ChunkPos cp) {
        if (mc.world == null) return false;
        int mossy = 0;
        for (int x = cp.getStartX(); x <= cp.getEndX(); x += 2) {
            for (int z = cp.getStartZ(); z <= cp.getEndZ(); z += 2) {
                for (int y = mc.world.getBottomY(); y < 80; y += 2) {
                    Block b = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (b == Blocks.MOSSY_COBBLESTONE || b == Blocks.SPAWNER) mossy++;
                    if (mossy >= 4) return true;
                }
            }
        }
        return false;
    }
}
