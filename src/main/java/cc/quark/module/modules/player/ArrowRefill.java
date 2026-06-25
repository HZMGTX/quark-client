package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class ArrowRefill extends Module {

    private final IntSetting minArrows = register(new IntSetting(
            "MinArrows", "Move arrows from inventory to hotbar when count drops below this", 16, 1, 64));

    public ArrowRefill() {
        super("ArrowRefill", "Moves arrows from inventory to hotbar when low", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        var inv = mc.player.getInventory();

        // Count total arrows in hotbar
        int hotbarArrows = 0;
        int hotbarSlotWithArrows = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = inv.getStack(i);
            if (s.getItem() == Items.ARROW) {
                hotbarArrows += s.getCount();
                hotbarSlotWithArrows = i;
            }
        }

        if (hotbarArrows >= minArrows.get()) return;

        // Find arrows in main inventory (slots 9-35) and move to hotbar
        for (int invSlot = 9; invSlot < 36; invSlot++) {
            ItemStack s = inv.getStack(invSlot);
            if (s.getItem() != Items.ARROW) continue;

            // Find an empty hotbar slot or the existing arrow slot
            int targetHotbar = hotbarSlotWithArrows != -1 ? hotbarSlotWithArrows : findEmptyHotbarSlot();
            if (targetHotbar == -1) targetHotbar = 8; // fallback to slot 8

            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    invSlot,
                    targetHotbar,
                    SlotActionType.SWAP,
                    mc.player);
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
