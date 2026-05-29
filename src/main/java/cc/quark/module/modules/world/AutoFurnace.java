package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoFurnace extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Block range to search for furnaces", 4, 1, 8));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between actions", 300, 100, 2000));

    private final TimerUtil timer = new TimerUtil();

    public AutoFurnace() {
        super("AutoFurnace", "Automatically inserts fuel/smeltables and retrieves results from nearby furnaces", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // If furnace screen is open, manage it
        if (mc.player.currentScreenHandler instanceof AbstractFurnaceScreenHandler handler) {
            int syncId = handler.syncId;
            // Slot 2 = output — take it first
            if (handler.slots.get(2).hasStack()) {
                mc.interactionManager.clickSlot(syncId, 2, 0, SlotActionType.QUICK_MOVE, mc.player);
                timer.reset();
                return;
            }
            // Try to fill slot 0 (input) or slot 1 (fuel) from inventory
            for (int slot = 0; slot <= 1; slot++) {
                if (!handler.slots.get(slot).hasStack()) {
                    for (int invSlot = 3; invSlot < handler.slots.size(); invSlot++) {
                        if (handler.slots.get(invSlot).hasStack()) {
                            mc.interactionManager.clickSlot(syncId, invSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                            timer.reset();
                            return;
                        }
                    }
                }
            }
            timer.reset();
            return;
        }

        // Look for nearby furnace to open
        BlockPos playerPos = mc.player.getBlockPos();
        int r = range.get();
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    var block = mc.world.getBlockState(pos).getBlock();
                    if (block == Blocks.FURNACE || block == Blocks.BLAST_FURNACE || block == Blocks.SMOKER) {
                        Vec3d hitVec = Vec3d.ofCenter(pos);
                        BlockHitResult hit = new BlockHitResult(hitVec, Direction.NORTH, pos, false);
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                        timer.reset();
                        return;
                    }
                }
            }
        }
    }
}
