package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.block.Blocks;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ChestRestock extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to look for chests to restock from (blocks)", 3.0, 1.0, 6.0));

    private final BoolSetting autoOpen = register(new BoolSetting(
            "AutoOpen", "Automatically open and take items from nearby chests", true));

    private boolean restocking = false;

    public ChestRestock() {
        super("ChestRestock", "Restocks items from nearby chests", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        restocking = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // If a chest screen is open, take everything with shift-click
        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler container) {
            if (restocking) {
                stealFromContainer(container);
            }
            return;
        }

        restocking = false;

        if (!autoOpen.isEnabled()) return;
        if (mc.currentScreen != null) return;

        // Look for a chest within range
        double r = range.get();
        BlockPos playerPos = mc.player.getBlockPos();

        for (int x = (int) -r; x <= r; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = (int) -r; z <= r; z++) {
                    BlockPos checkPos = playerPos.add(x, y, z);
                    if (!isChest(checkPos)) continue;

                    Vec3d chestCenter = Vec3d.ofCenter(checkPos);
                    if (chestCenter.distanceTo(mc.player.getEyePos()) > r + 1) continue;

                    // Open the chest
                    var direction = net.minecraft.util.math.Direction.UP;
                    var hitResult = new BlockHitResult(chestCenter, direction, checkPos, false);
                    mc.interactionManager.interactBlock(mc.player, net.minecraft.util.Hand.MAIN_HAND, hitResult);
                    restocking = true;
                    return;
                }
            }
        }
    }

    private boolean isChest(BlockPos pos) {
        var block = mc.world.getBlockState(pos).getBlock();
        return block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.BARREL;
    }

    private void stealFromContainer(GenericContainerScreenHandler container) {
        // Shift-click every non-empty slot in the chest (top portion)
        int chestSlots = container.getRows() * 9;
        for (int i = 0; i < chestSlots; i++) {
            if (!container.slots.get(i).getStack().isEmpty()) {
                mc.interactionManager.clickSlot(
                        container.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                return; // One item per tick
            }
        }

        // Chest is empty — close it
        mc.player.closeHandledScreen();
        restocking = false;
        ChatUtil.info("[ChestRestock] Chest emptied.");
    }
}
