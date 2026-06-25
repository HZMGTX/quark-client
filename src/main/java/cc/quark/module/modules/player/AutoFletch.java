package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * AutoFletch - detects when the player has feathers, flint, and sticks in their
 * inventory and notifies them / auto-moves materials together so arrows can be crafted.
 *
 * Full auto-crafting requires a crafting table interaction or a recipe handler;
 * this module handles the inventory detection/movement pass and alerts.
 */
public class AutoFletch extends Module {

    private final BoolSetting autoCraft = register(new BoolSetting(
            "AutoCraft", "Attempt to move materials to craft arrows automatically", true));

    public AutoFletch() {
        super("AutoFletch", "Auto-crafts arrows from feathers/flint/sticks", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (!autoCraft.isEnabled()) return;

        var inv = mc.player.getInventory();

        int feathers = countItem(Items.FEATHER);
        int flint    = countItem(Items.FLINT);
        int sticks   = countItem(Items.STICK);

        int batches = Math.min(feathers, Math.min(flint, sticks));
        if (batches <= 0) return;

        // Notify player how many arrows they could craft
        // (Actual crafting through a crafting table requires opening a screen)
        ChatUtil.info("[AutoFletch] Can craft " + (batches * 4) + " arrows from inventory materials.");

        // Consolidate materials to hotbar slots for quick crafting access
        moveToHotbarIfNeeded(Items.FEATHER, 5);
        moveToHotbarIfNeeded(Items.FLINT, 6);
        moveToHotbarIfNeeded(Items.STICK, 7);
    }

    private int countItem(net.minecraft.item.Item item) {
        var inv = mc.player.getInventory();
        int total = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getStack(i);
            if (s.getItem() == item) total += s.getCount();
        }
        return total;
    }

    private void moveToHotbarIfNeeded(net.minecraft.item.Item item, int hotbarSlot) {
        var inv = mc.player.getInventory();
        if (!inv.getStack(hotbarSlot).isEmpty()) return;

        for (int i = 9; i < 36; i++) {
            ItemStack s = inv.getStack(i);
            if (s.getItem() == item) {
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        i, hotbarSlot, SlotActionType.SWAP, mc.player);
                return;
            }
        }
    }
}
