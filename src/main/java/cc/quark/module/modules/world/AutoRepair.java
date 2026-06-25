package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

/**
 * AutoRepair - Automatically repairs damaged tools/armor when an anvil
 * GUI is open. Detects the most damaged item in the player's inventory,
 * places it in the anvil's left slot along with a matching material,
 * then collects the repaired result.
 */
public class AutoRepair extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between anvil actions", 300, 100, 2000));
    private final IntSetting threshold = register(new IntSetting(
            "Threshold", "Repair when durability % falls below this value", 30, 5, 95));

    private final TimerUtil timer = new TimerUtil();

    public AutoRepair() {
        super("AutoRepair", "Uses open anvil GUI to repair damaged tools and armor automatically", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        var handler = mc.player.currentScreenHandler;
        if (!(handler instanceof AnvilScreenHandler anvil)) return;
        if (!timer.hasReached(delay.get())) return;

        // Anvil slots: 0 = left item, 1 = right item/material, 2 = result
        boolean slot0HasItem = anvil.slots.get(0).hasStack();
        boolean slot1HasItem = anvil.slots.get(1).hasStack();
        boolean hasResult    = anvil.slots.get(2).hasStack();

        // Step 1: take result if available
        if (hasResult) {
            mc.interactionManager.clickSlot(anvil.syncId, 2, 0, SlotActionType.QUICK_MOVE, mc.player);
            timer.reset();
            return;
        }

        // Inventory slots in the anvil handler start at index 3
        int invStart = 3;
        int totalSlots = anvil.slots.size();

        // Step 2: place the most damaged item into slot 0
        if (!slot0HasItem) {
            int damagedSlot = findMostDamagedSlot(anvil, invStart, totalSlots);
            if (damagedSlot != -1) {
                mc.interactionManager.clickSlot(anvil.syncId, damagedSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                timer.reset();
                return;
            }
        }

        // Step 3: place a matching material/duplicate into slot 1
        if (slot0HasItem && !slot1HasItem) {
            ItemStack toRepair = anvil.slots.get(0).getStack();
            int materialSlot = findRepairMaterial(anvil, toRepair, invStart, totalSlots);
            if (materialSlot != -1) {
                mc.interactionManager.clickSlot(anvil.syncId, materialSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                timer.reset();
            }
        }
    }

    /** Returns the handler slot index of the most damaged item below the threshold. */
    private int findMostDamagedSlot(AnvilScreenHandler anvil, int start, int total) {
        int bestSlot = -1;
        float bestRatio = 0f;
        for (int i = start; i < total; i++) {
            ItemStack stack = anvil.slots.get(i).getStack();
            if (stack.isEmpty()) continue;
            int maxDmg = stack.getMaxDamage();
            if (maxDmg <= 0) continue;
            float pct = (float) stack.getDamage() / maxDmg * 100f;
            if (pct >= threshold.get() && pct > bestRatio) {
                bestRatio = pct;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    /**
     * Finds a repair material: either an identical item (for unit-repair) or
     * the raw repair material of the same tier (iron ingot for iron tools, etc.).
     */
    private int findRepairMaterial(AnvilScreenHandler anvil, ItemStack toRepair, int start, int total) {
        // Prefer identical item (unit repair gives +12% per item)
        for (int i = start; i < total; i++) {
            ItemStack stack = anvil.slots.get(i).getStack();
            if (!stack.isEmpty() && ItemStack.areItemsEqual(stack, toRepair)) return i;
        }
        // Fallback: any stack that is a valid repair ingredient via ToolMaterial (1.21.1 API)
        for (int i = start; i < total; i++) {
            ItemStack stack = anvil.slots.get(i).getStack();
            if (!stack.isEmpty() && toRepair.getItem() instanceof net.minecraft.item.ToolItem tool) {
                try {
                    var ingredient = tool.getMaterial().value().repairIngredient();
                    if (ingredient != null && ingredient.test(stack)) return i;
                } catch (Exception ignored) {}
            }
        }
        return -1;
    }
}
