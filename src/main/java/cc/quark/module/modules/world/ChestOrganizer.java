package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ChestOrganizer extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Organization method",
            "Sort", "Sort", "DumpAll", "StackMerge"));

    private final IntSetting range = register(new IntSetting(
            "Range", "Radius to search for chests", 4, 1, 8));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between slot actions", 150, 50, 1000));

    private final BoolSetting announceWhenDone = register(new BoolSetting(
            "Announce", "Print message when organization is complete", true));

    private final TimerUtil timer = new TimerUtil();
    private boolean acted = false;

    public ChestOrganizer() {
        super("ChestOrganizer", "Organizes nearby chest inventories by sorting or stacking items", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
        acted = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // If a chest is open, do the organization action
        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler handler) {
            int syncId = handler.syncId;
            int chestSlots = handler.getRows() * 9;

            switch (mode.get()) {
                case "Sort" -> {
                    // Find the first unsorted pair (stack in wrong slot order by item id)
                    // Simple bubble-sort pass: swap item in lower slot with higher slot if "smaller"
                    for (int i = 0; i < chestSlots - 1; i++) {
                        var slotA = handler.slots.get(i);
                        var slotB = handler.slots.get(i + 1);
                        if (!slotA.hasStack() && slotB.hasStack()) {
                            // Shift B into A
                            mc.interactionManager.clickSlot(syncId, i + 1, 0, SlotActionType.PICKUP, mc.player);
                            mc.interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, mc.player);
                            timer.reset();
                            acted = true;
                            return;
                        }
                        if (slotA.hasStack() && slotB.hasStack()) {
                            String nameA = slotA.getStack().getItem().toString();
                            String nameB = slotB.getStack().getItem().toString();
                            if (nameA.compareTo(nameB) > 0) {
                                // Swap
                                mc.interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, mc.player);
                                mc.interactionManager.clickSlot(syncId, i + 1, 0, SlotActionType.PICKUP, mc.player);
                                mc.interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, mc.player);
                                timer.reset();
                                acted = true;
                                return;
                            }
                        }
                    }
                    if (acted && announceWhenDone.isEnabled()) {
                        ChatUtil.info("ChestOrganizer: Sort complete.");
                        acted = false;
                    }
                }
                case "DumpAll" -> {
                    // Quick-move all player inventory items into the chest
                    int totalSlots = handler.slots.size();
                    for (int i = chestSlots; i < totalSlots; i++) {
                        if (handler.slots.get(i).hasStack()) {
                            mc.interactionManager.clickSlot(syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                            timer.reset();
                            acted = true;
                            return;
                        }
                    }
                    if (acted && announceWhenDone.isEnabled()) {
                        ChatUtil.info("ChestOrganizer: DumpAll complete.");
                        acted = false;
                    }
                }
                case "StackMerge" -> {
                    // Find two slots with the same item that aren't full, merge them
                    for (int i = 0; i < chestSlots; i++) {
                        var slotA = handler.slots.get(i);
                        if (!slotA.hasStack()) continue;
                        if (slotA.getStack().getCount() >= slotA.getStack().getMaxCount()) continue;
                        for (int j = i + 1; j < chestSlots; j++) {
                            var slotB = handler.slots.get(j);
                            if (!slotB.hasStack()) continue;
                            if (!slotA.getStack().getItem().equals(slotB.getStack().getItem())) continue;
                            mc.interactionManager.clickSlot(syncId, j, 0, SlotActionType.PICKUP, mc.player);
                            mc.interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, mc.player);
                            if (mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                                // fully merged
                            } else {
                                mc.interactionManager.clickSlot(syncId, j, 0, SlotActionType.PICKUP, mc.player);
                            }
                            timer.reset();
                            acted = true;
                            return;
                        }
                    }
                    if (acted && announceWhenDone.isEnabled()) {
                        ChatUtil.info("ChestOrganizer: StackMerge complete.");
                        acted = false;
                    }
                }
            }

            timer.reset();
            return;
        }

        // Open nearest chest
        BlockPos playerPos = mc.player.getBlockPos();
        int r = range.get();
        for (BlockPos pos : BlockPos.iterate(playerPos.add(-r, -r, -r), playerPos.add(r, r, r))) {
            var block = mc.world.getBlockState(pos).getBlock();
            if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.BARREL) {
                Vec3d hitVec = Vec3d.ofCenter(pos);
                BlockHitResult hit = new BlockHitResult(hitVec, Direction.NORTH, pos.toImmutable(), false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                timer.reset();
                return;
            }
        }

        timer.reset();
    }
}
