package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class PearlRefill extends Module {

    private final IntSetting minPearls = register(new IntSetting(
            "MinPearls", "Move ender pearls to hotbar when count drops below this", 2, 1, 16));

    public PearlRefill() {
        super("PearlRefill", "Refills ender pearls from inventory to hotbar", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        var inv = mc.player.getInventory();

        // Count pearls in hotbar
        int hotbarPearls = 0;
        int hotbarPearlSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = inv.getStack(i);
            if (s.getItem() == Items.ENDER_PEARL) {
                hotbarPearls += s.getCount();
                hotbarPearlSlot = i;
            }
        }

        if (hotbarPearls >= minPearls.get()) return;

        // Find pearls in main inventory
        for (int i = 9; i < 36; i++) {
            ItemStack s = inv.getStack(i);
            if (s.getItem() != Items.ENDER_PEARL) continue;

            int targetSlot = hotbarPearlSlot != -1 ? hotbarPearlSlot : findEmptyHotbarSlot();
            if (targetSlot == -1) targetSlot = 8;

            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    i, targetSlot, SlotActionType.SWAP, mc.player);
            return;
        }
    }

    private int findEmptyHotbarSlot() {
        var inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).isEmpty()) return i;
        }
        return -1;
    }
}
