package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ChunkSaver extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Chunk radius to scan for interesting content", 2, 1, 8));
    private final BoolSetting autoSave = register(new BoolSetting(
            "AutoSave", "Automatically save interesting chunks to file", true));

    private final TimerUtil scanTimer = new TimerUtil();
    private final Set<ChunkPos> savedChunks = new HashSet<>();

    public ChunkSaver() {
        super("ChunkSaver", "Scans nearby chunks for interesting content and saves coordinates to disk", Category.WORLD);
    }

    @Override
    public void onEnable() {
        savedChunks.clear();
        scanTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!scanTimer.hasReached(3000)) return;
        scanTimer.reset();

        ChunkPos playerChunk = new ChunkPos(mc.player.getBlockPos());
        int r = range.get();

        for (int cx = playerChunk.x - r; cx <= playerChunk.x + r; cx++) {
            for (int cz = playerChunk.z - r; cz <= playerChunk.z + r; cz++) {
                ChunkPos cp = new ChunkPos(cx, cz);
                if (savedChunks.contains(cp)) continue;
                if (mc.world.getChunk(cx, cz) == null) continue;

                int interest = scoreChunk(cp);
                if (interest < 5) continue;

                savedChunks.add(cp);
                String note = "Chunk [" + cx + "," + cz + "] interest=" + interest;
                ChatUtil.info("[ChunkSaver] " + note);
                if (autoSave.isEnabled()) writeToLog(note);
            }
        }
    }

    private int scoreChunk(ChunkPos cp) {
        if (mc.world == null) return 0;
        int score = 0;
        for (int x = cp.getStartX(); x <= cp.getEndX(); x += 2) {
            for (int z = cp.getStartZ(); z <= cp.getEndZ(); z += 2) {
                for (int y = mc.world.getBottomY(); y < mc.world.getTopY(); y += 2) {
                    var block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) score += 3;
                    else if (block == Blocks.ANCIENT_DEBRIS) score += 5;
                    else if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) score += 2;
                    else if (block == Blocks.SPAWNER) score += 4;
                    else if (block == Blocks.CHEST) score += 2;
                }
            }
        }
        return score;
    }

    private void writeToLog(String message) {
        try {
            Path dir = mc.runDirectory.toPath().resolve("quark");
            Files.createDirectories(dir);
            Path file = dir.resolve("chunks.log");
            String line = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + message + "\n";
            Files.write(file, line.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }
}
