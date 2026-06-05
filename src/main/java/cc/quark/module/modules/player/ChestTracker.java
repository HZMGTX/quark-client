package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.entity.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashMap;
import java.util.Map;

/**
 * ChestTracker - Remembers and renders all chests, barrels, and shulker boxes
 * that were within range at any point during the session. Draws ESP boxes
 * over their world positions.
 */
public class ChestTracker extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Scan Range", "Block scan range around the player", 64.0, 16.0, 128.0));

    private final BoolSetting trackBarrels = register(new BoolSetting(
            "Barrels", "Also track barrel blocks", true));

    private final BoolSetting trackShulkers = register(new BoolSetting(
            "Shulkers", "Also track shulker boxes", true));

    private final ColorSetting chestColor = register(new ColorSetting(
            "Chest Color", "Outline color for chests", 0xFFFFAA00));

    private final ColorSetting barrelColor = register(new ColorSetting(
            "Barrel Color", "Outline color for barrels", 0xFF00AAFF));

    private final ColorSetting shulkerColor = register(new ColorSetting(
            "Shulker Color", "Outline color for shulkers", 0xFFAA00FF));

    private final IntSetting scanDelay = register(new IntSetting(
            "Scan Delay", "Ticks between area scans", 20, 5, 100));

    // Map of BlockPos -> type label ("Chest", "Barrel", "Shulker")
    private final Map<BlockPos, String> tracked = new HashMap<>();
    private int scanTick = 0;

    public ChestTracker() {
        super("ChestTracker", "Tracks and highlights chests and containers found in range", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        tracked.clear();
        scanTick = 0;
    }

    @Override
    public void onDisable() {
        tracked.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        scanTick++;
        if (scanTick < scanDelay.get()) return;
        scanTick = 0;

        BlockPos playerPos = mc.player.getBlockPos();
        int chunkRadius = ((int) Math.ceil(range.get()) >> 4) + 1;
        ChunkPos playerChunk = new ChunkPos(playerPos);

        for (int cx = playerChunk.x - chunkRadius; cx <= playerChunk.x + chunkRadius; cx++) {
            for (int cz = playerChunk.z - chunkRadius; cz <= playerChunk.z + chunkRadius; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    BlockPos pos = be.getPos();
                    if (playerPos.getManhattanDistance(pos) > range.get() * 1.5) continue;

                    if (be instanceof ChestBlockEntity) {
                        tracked.put(pos, "Chest");
                    } else if (trackBarrels.isEnabled() && be instanceof BarrelBlockEntity) {
                        tracked.put(pos, "Barrel");
                    } else if (trackShulkers.isEnabled() && be instanceof ShulkerBoxBlockEntity) {
                        tracked.put(pos, "Shulker");
                    }
                }
            }
        }

        // Clean up entries whose block entities no longer exist
        tracked.entrySet().removeIf(entry -> {
            BlockPos pos = entry.getKey();
            ChunkPos cp = new ChunkPos(pos);
            WorldChunk c = mc.world.getChunkManager().getWorldChunk(cp.x, cp.z);
            return c == null || c.getBlockEntity(pos) == null;
        });
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;

        MatrixStack matrices = event.getMatrixStack();

        for (Map.Entry<BlockPos, String> entry : tracked.entrySet()) {
            BlockPos pos = entry.getKey();
            String label = entry.getValue();

            Box box = new Box(pos);

            float r, g, b;
            switch (label) {
                case "Barrel" -> {
                    r = barrelColor.getRedF(); g = barrelColor.getGreenF(); b = barrelColor.getBlueF();
                }
                case "Shulker" -> {
                    r = shulkerColor.getRedF(); g = shulkerColor.getGreenF(); b = shulkerColor.getBlueF();
                }
                default -> {
                    r = chestColor.getRedF(); g = chestColor.getGreenF(); b = chestColor.getBlueF();
                }
            }

            RenderUtil.drawESPBox(matrices, box, r, g, b, 0.9f, 1.5f);
            RenderUtil.drawFilledBox(matrices, box, r, g, b, 0.1f);
        }
    }
}
