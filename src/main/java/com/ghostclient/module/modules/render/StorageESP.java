package com.ghostclient.module.modules.render;

import com.ghostclient.GhostClient;
import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventRender3D;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.IntSetting;
import com.ghostclient.util.RenderUtil;
import net.minecraft.block.entity.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class StorageESP extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Search range", 32, 10, 64));
    private final BoolSetting showCount = register(new BoolSetting("Show Count", "Show item count", true));
    private final BoolSetting chests = register(new BoolSetting("Chests", "Show chests", true));
    private final BoolSetting shulkers = register(new BoolSetting("Shulkers", "Show shulker boxes", true));
    private final BoolSetting furnaces = register(new BoolSetting("Furnaces", "Show furnaces", false));

    public StorageESP() {
        super("StorageESP", "Shows storage blocks with item info", Category.RENDER, 0);
    }

    @Override
    public void onEnable() {
        GhostClient.getInstance().getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        GhostClient.getInstance().getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;
        MatrixStack matrices = event.getMatrixStack();

        mc.world.blockEntities.forEach(be -> {
            if (mc.player.getPos().distanceTo(be.getPos().toCenterPos()) > range.getValue()) return;
            int color = getColor(be);
            if (color == 0) return;
            BlockPos pos = be.getPos();
            Box box = new Box(pos.getX(), pos.getY(), pos.getZ(),
                              pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            RenderUtil.drawESPBox(matrices, box, color);
        });
    }

    private int getColor(BlockEntity be) {
        if (be instanceof ChestBlockEntity && chests.getValue()) return 0xFFFFAA00;
        if (be instanceof BarrelBlockEntity && chests.getValue()) return 0xFFAA7700;
        if (be instanceof ShulkerBoxBlockEntity && shulkers.getValue()) return 0xFFAA55FF;
        if (be instanceof FurnaceBlockEntity && furnaces.getValue()) return 0xFFFF5500;
        if (be instanceof BlastFurnaceBlockEntity && furnaces.getValue()) return 0xFFFF7700;
        if (be instanceof HopperBlockEntity) return 0xFF888888;
        return 0;
    }
}
