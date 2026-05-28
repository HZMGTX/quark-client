package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoHotbar extends Module {

    private final BoolSetting onlyWhenEmpty = register(new BoolSetting("Only When Empty", "Only fill empty hotbar slots", false));
    private final BoolSetting bestTool = register(new BoolSetting("Best Tool", "Switch to fastest mining tool for targeted block", true));

    public AutoHotbar() {
        super("AutoHotbar", "Auto-selects best tool/weapon for current context", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (bestTool.isEnabled() && mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
            BlockState state = mc.world.getBlockState(hit.getBlockPos());
            if (!state.isAir()) {
                float bestSpeed = -1f;
                int bestSlot = mc.player.getInventory().selectedSlot;
                for (int i = 0; i < 9; i++) {
                    if (onlyWhenEmpty.isEnabled() && !mc.player.getInventory().getStack(i).isEmpty()) continue;
                    float speed = mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(state);
                    if (speed > bestSpeed) {
                        bestSpeed = speed;
                        bestSlot = i;
                    }
                }
                mc.player.getInventory().selectedSlot = bestSlot;
                return;
            }
        }

        assignHotbarItems();
    }

    private void assignHotbarItems() {
        for (int invSlot = 9; invSlot < 36; invSlot++) {
            ItemStack stack = mc.player.getInventory().getStack(invSlot);
            if (stack.isEmpty()) continue;

            int targetHotbar = getTargetSlot(stack);
            if (targetHotbar == -1) continue;

            ItemStack existing = mc.player.getInventory().getStack(targetHotbar);
            if (onlyWhenEmpty.isEnabled() && !existing.isEmpty()) continue;
            if (!existing.isEmpty() && getTargetSlot(existing) == targetHotbar) continue;

            if (mc.interactionManager != null) {
                mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    invSlot, targetHotbar, net.minecraft.screen.slot.SlotActionType.SWAP, mc.player
                );
                return;
            }
        }
    }

    private int getTargetSlot(ItemStack stack) {
        if (stack.getItem() instanceof SwordItem) return 0;
        if (stack.getItem() instanceof PickaxeItem || stack.getItem() instanceof AxeItem || stack.getItem() instanceof ShovelItem) return 1;
        if (stack.getItem() instanceof BlockItem) return 3;
        return -1;
    }
}
