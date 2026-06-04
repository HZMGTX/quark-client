package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;

public class InventoryRefill2 extends Module {

    private final BoolSetting tools = register(new BoolSetting(
            "Tools", "Refill broken tools from inventory", true));

    private final BoolSetting armor = register(new BoolSetting(
            "Armor", "Equip better armor pieces from inventory", true));

    private final TimerUtil timer = new TimerUtil();
    private static final long CHECK_INTERVAL = 1000L;

    public InventoryRefill2() {
        super("InventoryRefill2", "Refills tools/armor/weapons from inventory", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (!timer.hasReached(CHECK_INTERVAL)) return;
        timer.reset();

        if (tools.isEnabled()) refillTools();
        if (armor.isEnabled()) refillArmor();
    }

    private void refillTools() {
        // Check hotbar for nearly-broken tools (< 5% durability)
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !stack.isDamageable()) continue;
            float pct = 1f - (float) stack.getDamage() / stack.getMaxDamage();
            if (pct > 0.05f) continue;

            // Find replacement in inventory (slots 9-35)
            for (int j = 9; j < 36; j++) {
                ItemStack candidate = mc.player.getInventory().getStack(j);
                if (candidate.isEmpty()) continue;
                if (candidate.getItem().getClass() == stack.getItem().getClass()) {
                    // Swap via click
                    int syncId = mc.player.currentScreenHandler.syncId;
                    mc.interactionManager.clickSlot(syncId, j, i, SlotActionType.SWAP, mc.player);
                    break;
                }
            }
        }
    }

    private void refillArmor() {
        // Armor slots: 36=boots, 37=leggings, 38=chestplate, 39=helmet
        int[] armorSlots = {36, 37, 38, 39};
        // Use ArmorItem.Type to match armor pieces correctly (no separate subclasses in 1.21.1)
        ArmorItem.Type[] armorTypes = {ArmorItem.Type.BOOTS, ArmorItem.Type.LEGGINGS,
                ArmorItem.Type.CHESTPLATE, ArmorItem.Type.HELMET};

        for (int ai = 0; ai < armorSlots.length; ai++) {
            ItemStack current = mc.player.getInventory().getStack(armorSlots[ai]);
            int currentProtection = getProtection(current);

            for (int j = 9; j < 36; j++) {
                ItemStack candidate = mc.player.getInventory().getStack(j);
                if (candidate.isEmpty()) continue;
                if (!(candidate.getItem() instanceof ArmorItem candidateArmor)) continue;
                if (candidateArmor.getType() != armorTypes[ai]) continue;

                int candidateProtection = getProtection(candidate);
                if (candidateProtection > currentProtection) {
                    int syncId = mc.player.currentScreenHandler.syncId;
                    // Swap candidate to armor slot
                    mc.interactionManager.clickSlot(syncId, j, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(syncId, armorSlots[ai] - 36 + 5, 0, SlotActionType.PICKUP, mc.player);
                    if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                        mc.interactionManager.clickSlot(syncId, j, 0, SlotActionType.PICKUP, mc.player);
                    }
                    break;
                }
            }
        }
    }

    private int getProtection(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) return 0;
        return armor.getProtection();
    }
}
