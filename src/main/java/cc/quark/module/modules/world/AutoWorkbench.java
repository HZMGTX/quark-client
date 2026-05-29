package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoWorkbench extends Module {

    private final ModeSetting recipe = register(new ModeSetting(
            "Recipe", "Item to auto-craft",
            "Sticks", "Sticks", "Planks", "Torches", "Chests"));

    private final TimerUtil timer = new TimerUtil();
    private boolean opened = false;

    public AutoWorkbench() {
        super("AutoWorkbench", "Auto-crafts configured recipes using a nearby workbench", Category.WORLD);
    }

    @Override
    public void onEnable() {
        opened = false;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(250)) return;

        if (mc.player.currentScreenHandler instanceof CraftingScreenHandler) {
            handleCraftingScreen();
            timer.reset();
            return;
        }

        if (opened) { opened = false; return; }

        BlockPos playerPos = mc.player.getBlockPos();
        for (int x = -4; x <= 4; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.CRAFTING_TABLE) {
                        Vec3d hitVec = Vec3d.ofCenter(pos);
                        BlockHitResult hit = new BlockHitResult(hitVec, Direction.NORTH, pos, false);
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                        opened = true;
                        timer.reset();
                        return;
                    }
                }
            }
        }
    }

    private void handleCraftingScreen() {
        var handler = mc.player.currentScreenHandler;
        if (handler == null) return;

        // Crafting screen: slot 0 = result, slots 1-9 = grid, slots 10+ = inventory
        if (handler.slots.size() > 0 && handler.slots.get(0).hasStack()) {
            mc.interactionManager.clickSlot(handler.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
            return;
        }

        // Move first available inventory item into the grid
        for (int invSlot = 10; invSlot < handler.slots.size(); invSlot++) {
            if (handler.slots.get(invSlot).hasStack()) {
                mc.interactionManager.clickSlot(handler.syncId, invSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                return;
            }
        }
    }
}
