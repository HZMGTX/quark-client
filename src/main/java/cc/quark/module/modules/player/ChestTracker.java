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
import net.minecraft.block.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

/**
 * ChestTracker - Remembers and renders all chests, barrels, and shulker boxes
 * that were within range at any point during the session. Draws ESP boxes
 * and labels over their world positions.
 */
public class ChestTracker extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Scan Range", "Block scan range around the player", 32.0, 8.0, 64.0));

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

    private final BoolSetting showLabels = register(new BoolSetting(
            "Labels", "Show block type label above each tracked container", true));

    private final IntSetting scanDelay = register(new IntSetting(
            "Scan Delay", "Ticks between area scans", 20, 5, 100));

    // Map of BlockPos -> type label
    private final Map<BlockPos, String> tracked = new HashMap<>();
    private int scanTick = 0;

    public ChestTracker() {
        super("ChestTracker", "Tracks and highlights opened chests and containers in range", Category.PLAYER);
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

        BlockPos center = mc.player.getBlockPos();
        int r = (int) Math.ceil(range.get());

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    if (mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) > range.get()) continue;

                    Block block = mc.world.getBlockState(pos).getBlock();
                    if (block instanceof ChestBlock || block instanceof TrappedChestBlock) {
                        tracked.put(pos, "Chest");
                    } else if (trackBarrels.isEnabled() && block instanceof BarrelBlock) {
                        tracked.put(pos, "Barrel");
                    } else if (trackShulkers.isEnabled() && block instanceof ShulkerBoxBlock) {
                        tracked.put(pos, "Shulker");
                    }
                }
            }
        }

        // Clean up entries that are no longer valid blocks (e.g., broken)
        tracked.entrySet().removeIf(entry -> {
            Block b = mc.world.getBlockState(entry.getKey()).getBlock();
            return !(b instanceof ChestBlock) && !(b instanceof TrappedChestBlock)
                    && !(b instanceof BarrelBlock) && !(b instanceof ShulkerBoxBlock);
        });
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;

        MatrixStack matrices = event.getMatrixStack();

        for (Map.Entry<BlockPos, String> entry : tracked.entrySet()) {
            BlockPos pos = entry.getKey();
            String label = entry.getValue();

            Box box = new Box(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0);

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
