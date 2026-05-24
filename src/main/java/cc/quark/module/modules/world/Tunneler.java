package cc.quark.module.modules.world;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.InventoryUtil;
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
        Quark.getInstance().getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        Quark.getInstance().getEventBus().unsubscribe(this);
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
