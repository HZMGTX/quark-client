package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class AutoSmith extends Module {

    private final BoolSetting autoUpgrade = register(new BoolSetting(
            "AutoUpgrade", "Automatically insert items and click the upgrade when a smithing table is open", true));

    public AutoSmith() {
        super("AutoSmith", "Auto-uses smithing table for upgrades", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (!autoUpgrade.isEnabled()) return;

        // If a smithing screen is already open, attempt to click the result slot
        if (mc.currentScreen != null && mc.player.currentScreenHandler instanceof SmithingScreenHandler smithing) {
            attemptSmithingUpgrade(smithing);
            return;
        }

        // Otherwise, look for a nearby smithing table to open
        if (mc.currentScreen != null) return;

        var hitResult = mc.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult blockHit)) return;

        BlockPos pos = blockHit.getBlockPos();
        if (mc.world.getBlockState(pos).getBlock() != Blocks.SMITHING_TABLE) return;

        mc.interactionManager.interactBlock(mc.player,
                net.minecraft.util.Hand.MAIN_HAND, blockHit);
    }

    private void attemptSmithingUpgrade(SmithingScreenHandler smithing) {
        // Slot layout (1.21): 0=template, 1=base, 2=addition, 3=result
        ItemStack template = smithing.slots.get(0).getStack();
        ItemStack base     = smithing.slots.get(1).getStack();
        ItemStack addition = smithing.slots.get(2).getStack();
        ItemStack result   = smithing.slots.get(3).getStack();

        // Try to fill empty template slot (netherite upgrade template)
        if (template.isEmpty()) {
            insertFromInventory(smithing, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 0);
            return;
        }

        // Try to fill base armor/tool slot
        if (base.isEmpty()) {
            insertBestArmor(smithing);
            return;
        }

        // Try to fill addition slot (netherite ingot)
        if (addition.isEmpty()) {
            insertFromInventory(smithing, Items.NETHERITE_INGOT, 2);
            return;
        }

        // If result is ready, take it
        if (!result.isEmpty()) {
            mc.interactionManager.clickSlot(
                    smithing.syncId, 3, 0, SlotActionType.QUICK_MOVE, mc.player);
            ChatUtil.success("[AutoSmith] Upgraded item!");
        }
    }

    private void insertFromInventory(SmithingScreenHandler smithing, net.minecraft.item.Item item, int targetSlot) {
        var inv = mc.player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getStack(i);
            if (s.getItem() != item) continue;

            int screenSlot = i < 9 ? 49 + i : i + 4; // approximate slot mapping
            mc.interactionManager.clickSlot(smithing.syncId, screenSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
            return;
        }
    }

    private void insertBestArmor(SmithingScreenHandler smithing) {
        var inv = mc.player.getInventory();
        net.minecraft.item.Item[] diamondArmor = {
            Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE,
            Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
            Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE,
            Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL
        };

        for (net.minecraft.item.Item target : diamondArmor) {
            for (int i = 0; i < 36; i++) {
                ItemStack s = inv.getStack(i);
                if (s.getItem() == target) {
                    int screenSlot = i < 9 ? 49 + i : i + 4;
                    mc.interactionManager.clickSlot(smithing.syncId, screenSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                    return;
                }
            }
        }
    }
}
