package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.RenderUtil;
import net.minecraft.block.entity.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class StashFinder extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Search radius in blocks", 32, 8, 64));

    private final IntSetting chestThreshold = register(new IntSetting(
            "Chest Threshold", "Min containers per chunk to flag as stash", 5, 1, 20));

    private final BoolSetting logToFile = register(new BoolSetting(
            "Log to File", "Write stash coordinates to quark/stashes.log", true));

    private final BoolSetting chatAlert = register(new BoolSetting(
            "Chat Alert", "Print stash coordinates in chat", true));

    private final BoolSetting announceNew = register(new BoolSetting(
            "Announce", "Print in chat when new containers are found", true));

    private final List<BlockPos> found = new ArrayList<>();
    private final Set<ChunkPos> flaggedChunks = new HashSet<>();
    private int scanTicker = 0;

    public StashFinder() {
        super("StashFinder", "Highlights nearby storage containers and finds stashes", Category.WORLD);
    }

    @Override
    public void onEnable() {
        found.clear();
        flaggedChunks.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (++scanTicker < 20) return;
        scanTicker = 0;

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();
        List<BlockPos> newFound = new ArrayList<>();

        Map<ChunkPos, List<BlockPos>> containersByChunk = new HashMap<>();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            BlockEntity be = mc.world.getBlockEntity(pos);
            if (be == null) continue;
            if (isContainer(be)) {
                BlockPos immutable = pos.toImmutable();
                newFound.add(immutable);
                ChunkPos chunkPos = new ChunkPos(pos);
                containersByChunk.computeIfAbsent(chunkPos, k -> new ArrayList<>()).add(immutable);
            }
        }

        for (Map.Entry<ChunkPos, List<BlockPos>> entry : containersByChunk.entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            List<BlockPos> containers = entry.getValue();
            if (containers.size() >= chestThreshold.get() && !flaggedChunks.contains(chunkPos)) {
                flaggedChunks.add(chunkPos);
                BlockPos firstPos = containers.get(0);
                String msg = "Stash at chunk " + chunkPos.x + "," + chunkPos.z
                        + " (" + firstPos.getX() + " " + firstPos.getY() + " " + firstPos.getZ() + ")"
                        + " - " + containers.size() + " containers";
                if (chatAlert.isEnabled()) {
                    ChatUtil.info(msg);
                }
                if (logToFile.isEnabled()) {
                    writeToLog(msg);
                }
            }
        }

        if (announceNew.isEnabled() && newFound.size() != found.size()) {
            ChatUtil.info("StashFinder: " + newFound.size() + " container(s) nearby.");
        }
        found.clear();
        found.addAll(newFound);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (found.isEmpty()) return;
        MatrixStack matrices = event.getMatrixStack();
        for (BlockPos pos : found) {
            Box box = new Box(pos);
            RenderUtil.drawESPBox(matrices, box, 0.9f, 0.65f, 0.1f, 0.9f, 1.5f);
            RenderUtil.drawFilledBox(matrices, box, 0.9f, 0.65f, 0.1f, 0.12f);
        }
    }

    private boolean isContainer(BlockEntity be) {
        return be instanceof ChestBlockEntity
                || be instanceof ShulkerBoxBlockEntity
                || be instanceof BarrelBlockEntity
                || be instanceof HopperBlockEntity
                || be instanceof DispenserBlockEntity
                || be instanceof DropperBlockEntity
                || be instanceof FurnaceBlockEntity
                || be instanceof BlastFurnaceBlockEntity
                || be instanceof SmokerBlockEntity;
    }

    private void writeToLog(String message) {
        try {
            Path logDir = mc.runDirectory.toPath().resolve("quark");
            Files.createDirectories(logDir);
            Path logFile = logDir.resolve("stashes.log");
            String line = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + message + "\n";
            Files.write(logFile, line.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
        }
    }
}
