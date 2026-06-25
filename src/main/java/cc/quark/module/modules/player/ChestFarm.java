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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ChestFarm extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Radius to search for chests to loot (blocks)", 3.0, 1.0, 6.0));

    private final BoolSetting autoClose = register(new BoolSetting(
            "AutoClose", "Auto-close the chest after looting all items", true));

    private boolean looting = false;

    public ChestFarm() {
        super("ChestFarm", "Auto-farms loot from chests", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        looting = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Loot open chest
        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler container) {
            looting = true;
            lootContainer(container);
            return;
        }

        looting = false;
        if (mc.currentScreen != null) return;

        // Find and open nearest chest
        double r = range.get();
        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos nearestChest = null;
        double nearestDist = Double.MAX_VALUE;

        for (int x = (int) -r; x <= r; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = (int) -r; z <= r; z++) {
                    BlockPos check = playerPos.add(x, y, z);
                    if (!isChest(check)) continue;
                    double dist = Vec3d.ofCenter(check).squaredDistanceTo(mc.player.getEyePos());
                    if (dist < nearestDist) {
                        nearestDist = dist;
                        nearestChest = check;
                    }
                }
            }
        }

        if (nearestChest == null) return;

        Vec3d chestCenter = Vec3d.ofCenter(nearestChest);
        if (chestCenter.distanceTo(mc.player.getEyePos()) > r + 1) return;

        BlockHitResult hit = new BlockHitResult(chestCenter, Direction.UP, nearestChest, false);
        mc.interactionManager.interactBlock(mc.player, net.minecraft.util.Hand.MAIN_HAND, hit);
    }

    private boolean isChest(BlockPos pos) {
        var block = mc.world.getBlockState(pos).getBlock();
        return block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.BARREL;
    }

    private void lootContainer(GenericContainerScreenHandler container) {
        int chestSlots = container.getRows() * 9;

        for (int i = 0; i < chestSlots; i++) {
            if (!container.slots.get(i).getStack().isEmpty()) {
                mc.interactionManager.clickSlot(
                        container.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                return; // One slot per tick
            }
        }

        // All slots empty — chest is fully looted
        if (autoClose.isEnabled()) {
            mc.player.closeHandledScreen();
            ChatUtil.info("[ChestFarm] Chest looted and closed.");
        }
    }
}
