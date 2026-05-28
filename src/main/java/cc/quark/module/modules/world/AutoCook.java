package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoCook extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Scan radius for furnaces/smokers", 4, 1, 8));

    private final TimerUtil timer = new TimerUtil();

    private static final int SLOT_INPUT  = 0;
    private static final int SLOT_FUEL   = 1;
    private static final int SLOT_OUTPUT = 2;

    public AutoCook() {
        super("AutoCook", "Auto-places raw food in furnaces and collects cooked output", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(200)) return;
        timer.reset();

        if (mc.player.currentScreenHandler instanceof AbstractFurnaceScreenHandler handler) {
            int syncId = handler.syncId;
            int total = handler.slots.size();
            int playerStart = total - 36;

            if (!handler.getSlot(SLOT_OUTPUT).getStack().isEmpty()) {
                mc.interactionManager.clickSlot(syncId, SLOT_OUTPUT, 0, SlotActionType.QUICK_MOVE, mc.player);
                return;
            }

            if (handler.getSlot(SLOT_FUEL).getStack().isEmpty()) {
                for (int i = playerStart; i < total; i++) {
                    if (isFuel(handler.slots.get(i).getStack().getItem())) {
                        mc.interactionManager.clickSlot(syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                        return;
                    }
                }
            }

            if (handler.getSlot(SLOT_INPUT).getStack().isEmpty()) {
                for (int i = playerStart; i < total; i++) {
                    if (isRawFood(handler.slots.get(i).getStack().getItem())) {
                        mc.interactionManager.clickSlot(syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                        return;
                    }
                }
            }
            return;
        }

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            var bs = mc.world.getBlockState(pos);
            if (!bs.isOf(Blocks.FURNACE) && !bs.isOf(Blocks.SMOKER)) continue;
            if (mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)) > (r + 1.0) * (r + 1.0)) continue;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            return;
        }
    }

    private boolean isRawFood(Item item) {
        return item == Items.RAW_BEEF || item == Items.RAW_CHICKEN || item == Items.RAW_PORKCHOP
                || item == Items.RAW_MUTTON || item == Items.RAW_RABBIT
                || item == Items.RAW_SALMON || item == Items.RAW_COD || item == Items.POTATO;
    }

    private boolean isFuel(Item item) {
        return item == Items.COAL || item == Items.CHARCOAL || item == Items.COAL_BLOCK
                || item == Items.OAK_LOG || item == Items.SPRUCE_LOG || item == Items.BIRCH_LOG
                || item == Items.JUNGLE_LOG || item == Items.ACACIA_LOG || item == Items.DARK_OAK_LOG
                || item == Items.BLAZE_ROD || item == Items.LAVA_BUCKET;
    }
}
