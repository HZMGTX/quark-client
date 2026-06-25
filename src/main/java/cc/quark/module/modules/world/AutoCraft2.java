package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.Blocks;

public class AutoCraft2 extends Module {

    private final IntSetting craftCount = register(new IntSetting(
            "Count", "Times to craft (0 = unlimited)", 0, 0, 256));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between craft actions", 200, 50, 2000));

    private final BoolSetting openNearby = register(new BoolSetting(
            "OpenNearby", "Automatically open nearby crafting table", true));

    private final BoolSetting shiftClick = register(new BoolSetting(
            "ShiftClick", "Shift-click result to collect stack", true));

    private final TimerUtil timer = new TimerUtil();
    private int craftsDone = 0;
    private boolean announced = false;

    public AutoCraft2() {
        super("AutoCraft2", "Queues and executes crafting recipes automatically", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
        craftsDone = 0;
        announced = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        int target = craftCount.get();
        if (target > 0 && craftsDone >= target) {
            if (!announced) {
                ChatUtil.info("AutoCraft2: Finished " + craftsDone + " crafts.");
                announced = true;
            }
            return;
        }

        // If crafting screen is open, try to take the result
        if (mc.player.currentScreenHandler instanceof CraftingScreenHandler handler) {
            // Slot 0 is the output slot in a crafting table handler
            if (handler.slots.get(0).hasStack()) {
                int syncId = handler.syncId;
                if (shiftClick.isEnabled()) {
                    mc.interactionManager.clickSlot(syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
                } else {
                    mc.interactionManager.clickSlot(syncId, 0, 0, SlotActionType.PICKUP, mc.player);
                    // Drop back into inventory via another click
                    mc.interactionManager.clickSlot(syncId, 0, 0, SlotActionType.PICKUP, mc.player);
                }
                craftsDone++;
                timer.reset();
                return;
            }
            // No result ready yet — ingredients missing or not arranged
            timer.reset();
            return;
        }

        // Try to open a nearby crafting table
        if (openNearby.isEnabled()) {
            BlockPos playerPos = mc.player.getBlockPos();
            for (int r = 1; r <= 4; r++) {
                for (BlockPos pos : BlockPos.iterate(playerPos.add(-r, -1, -r), playerPos.add(r, 1, r))) {
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.CRAFTING_TABLE) {
                        Vec3d hitVec = Vec3d.ofCenter(pos);
                        BlockHitResult hit = new BlockHitResult(hitVec, Direction.NORTH, pos.toImmutable(), false);
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                        timer.reset();
                        return;
                    }
                }
            }
        }

        timer.reset();
    }
}
