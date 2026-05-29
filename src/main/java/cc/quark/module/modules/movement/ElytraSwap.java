package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

/**
 * ElytraSwap - press a configurable key to swap between the equipped chest
 * armour and an elytra found in the inventory (or vice versa).
 *
 * <p>The logic:
 * <ol>
 *   <li>If an elytra is currently in the chest slot, search inventory for a
 *       chestplate and swap them.</li>
 *   <li>If a chestplate is in the chest slot (or chest is empty), search
 *       inventory for an elytra and quick-move it to the chest slot.</li>
 * </ol>
 */
public class ElytraSwap extends Module {

    private final IntSetting swapKey = register(new IntSetting(
            "Swap Key", "GLFW key code to trigger the swap", GLFW.GLFW_KEY_G, 0, 400));

    /** PlayerScreenHandler chest armour slot index. */
    private static final int CHEST_SLOT = 6;

    public ElytraSwap() {
        super("ElytraSwap", "Hotkey swap between elytra and chestplate", Category.MOVEMENT);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (event.getKeyCode() != swapKey.get()) return;

        ItemStack equipped = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        int syncId = mc.player.playerScreenHandler.syncId;

        if (equipped.getItem() == Items.ELYTRA) {
            // Currently wearing elytra — find best chestplate in inventory
            int chestSlot = findChestplate();
            if (chestSlot != -1) {
                // Click chest slot to pick up elytra, then place in inventory
                mc.interactionManager.clickSlot(syncId, CHEST_SLOT, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(syncId, chestSlot,  0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(syncId, CHEST_SLOT, 0, SlotActionType.PICKUP, mc.player);
            } else {
                // No chestplate found — just quick-move elytra to inventory
                mc.interactionManager.clickSlot(syncId, CHEST_SLOT, 0, SlotActionType.QUICK_MOVE, mc.player);
            }
        } else {
            // Currently wearing chestplate (or nothing) — find elytra
            int elytraSlot = findElytra();
            if (elytraSlot == -1) return;

            if (!equipped.isEmpty()) {
                // Swap current chestplate with elytra
                mc.interactionManager.clickSlot(syncId, CHEST_SLOT,  0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(syncId, elytraSlot,  0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(syncId, CHEST_SLOT,  0, SlotActionType.PICKUP, mc.player);
            } else {
                // Chest slot empty — quick-move elytra there
                mc.interactionManager.clickSlot(syncId, elytraSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
            }
        }
    }

    private int findElytra() {
        if (mc.player == null) return -1;
        for (int i = 9; i <= 44; i++) {
            ItemStack s = mc.player.getInventory().getStack(i - 9);
            if (s.getItem() == Items.ELYTRA) return i;
        }
        // Also check hotbar
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ELYTRA) return i + 36;
        }
        return -1;
    }

    private int findChestplate() {
        if (mc.player == null) return -1;
        for (int i = 9; i <= 35; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty()) continue;
            String name = s.getItem().toString();
            if (name.contains("chestplate")) return i;
        }
        return -1;
    }
}
