package com.ghostclient.module.modules.world;

import com.ghostclient.GhostClient;
import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.IntSetting;
import com.ghostclient.util.InventoryUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Tunneler extends Module {

    private final IntSetting height = register(new IntSetting("Height", "Tunnel height", 2, 1, 3));
    private final IntSetting width = register(new IntSetting("Width", "Tunnel width", 1, 1, 3));

    public Tunneler() {
        super("Tunneler", "Automatically mines a tunnel", Category.WORLD, 0);
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
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        int pickSlot = InventoryUtil.findBestPickaxe();
        if (pickSlot == -1) return;
        mc.player.getInventory().selectedSlot = pickSlot < 9 ? pickSlot : mc.player.getInventory().selectedSlot;

        BlockPos base = mc.player.getBlockPos();
        Direction facing = mc.player.getHorizontalFacing();

        for (int h = 0; h < height.getValue(); h++) {
            for (int w = -(width.getValue()/2); w <= width.getValue()/2; w++) {
                BlockPos pos = base.offset(facing).up(h);
                if (facing == Direction.NORTH || facing == Direction.SOUTH)
                    pos = pos.east(w);
                else
                    pos = pos.north(w);

                if (!mc.world.getBlockState(pos).isAir()) {
                    mc.interactionManager.updateBlockBreakingProgress(pos, facing.getOpposite());
                }
            }
        }
    }
}
