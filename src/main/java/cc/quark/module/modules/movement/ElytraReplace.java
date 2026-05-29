package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * ElytraReplace - watches the equipped elytra's durability and automatically
 * swaps it for a fresh one from the inventory when it drops below a threshold.
 *
 * <p>The chest armour slot index in the player screen handler is 6.
 * Inventory slots 9-35 are searched for a replacement elytra.
 */
public class ElytraReplace extends Module {

    private final IntSetting durabilityThreshold = register(new IntSetting(
            "Durability", "Swap when remaining elytra durability drops to or below this", 50, 1, 432));
    private final BoolSetting onlyFlying = register(new BoolSetting(
            "Only Flying", "Only monitor durability while actually gliding", false));

    /** Player screen handler slot index for the chest armour (elytra) slot. */
    private static final int CHEST_SLOT = 6;

    public ElytraReplace() {
        super("ElytraReplace", "Auto-swap elytra when durability is low", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        if (onlyFlying.isEnabled() && !mc.player.isFallFlying()) return;

        ItemStack equipped = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        if (equipped.isEmpty() || equipped.getItem() != Items.ELYTRA) return;

        int maxDurability = equipped.getMaxDamage();
        int damage        = equipped.getDamage();
        int remaining     = maxDurability - damage;

        if (remaining > durabilityThreshold.get()) return;

        // Search inventory slots 9-35 for a fresh elytra
        int freshSlot = -1;
        for (int i = 9; i <= 35; i++) {
            ItemStack candidate = mc.player.getInventory().getStack(i);
            if (candidate.isEmpty() || candidate.getItem() != Items.ELYTRA) continue;
            int candRemaining = candidate.getMaxDamage() - candidate.getDamage();
            if (candRemaining > durabilityThreshold.get()) {
                freshSlot = i;
                break;
            }
        }

        if (freshSlot == -1) return;

        // Shift-click the fresh elytra to auto-equip it to the chest slot
        // The correct screen handler index: inventory row slots start at 9 in PlayerScreenHandler
        // slot 9 in inventory maps to slot index 9+27-... we use clickSlot directly
        int syncId = mc.player.playerScreenHandler.syncId;
        // Offset: slots 0-8 are crafting/result/armor, slots 9+ are inventory
        // In PlayerScreenHandler: 0=result, 1-4=crafting, 5=helmet, 6=chest, 7=legs, 8=boots, 9=offhand,
        // then 9..35 become indices 9..35 directly on the playerScreenHandler
        mc.interactionManager.clickSlot(syncId, freshSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
    }
}
