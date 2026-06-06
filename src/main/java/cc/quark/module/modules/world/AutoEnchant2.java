package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

/**
 * AutoEnchant2 — automatically enchants items at an enchanting table.
 * Places item in enchant slot, inserts lapis, and clicks the selected enchant level.
 */
public class AutoEnchant2 extends Module {

    private final IntSetting enchantLevel = register(new IntSetting(
            "Enchant Level", "Which enchantment option to click (1=cheapest, 3=most expensive)", 3, 1, 3));
    private final IntSetting minLapis = register(new IntSetting(
            "Min Lapis", "Minimum lapis in slot before enchanting", 3, 1, 64));
    private final BoolSetting autoLapis = register(new BoolSetting(
            "Auto Lapis", "Automatically insert lapis lazuli from inventory", true));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between actions", 300, 100, 2000));

    private final TimerUtil timer = new TimerUtil();
    private int step = 0;

    public AutoEnchant2() {
        super("AutoEnchant2", "Automatically enchants items at enchanting table", Category.WORLD);
    }

    @Override
    public void onEnable() {
        step = 0;
        timer.reset();
    }

    private int findInInventory(net.minecraft.item.Item item) {
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!(mc.currentScreen instanceof EnchantmentScreen)) return;
        if (!timer.hasReached(delay.get())) return;

        EnchantmentScreenHandler handler = (EnchantmentScreenHandler) mc.player.currentScreenHandler;

        // Slot 0 = item to enchant, Slot 1 = lapis slot (in enchantment screen handler)
        ItemStack enchantSlot = handler.getSlot(0).getStack();
        ItemStack lapisSlot   = handler.getSlot(1).getStack();

        // Step 0: ensure item is in the enchant slot
        if (enchantSlot.isEmpty()) {
            // Find an unenchanted item in hotbar/inventory and move it
            for (int i = 2; i < handler.slots.size(); i++) {
                var s = handler.getSlot(i).getStack();
                if (!s.isEmpty() && !s.isOf(Items.LAPIS_LAZULI) && !s.hasEnchantments()) {
                    mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    timer.reset();
                    return;
                }
            }
            return;
        }

        // Step 1: ensure lapis is in the lapis slot
        if (autoLapis.isEnabled() && (lapisSlot.isEmpty() || lapisSlot.getCount() < minLapis.get())) {
            int lapisInv = findInInventory(Items.LAPIS_LAZULI);
            if (lapisInv >= 0) {
                // Convert inventory index to screen slot index (offset by 2 for enchant table slots)
                int screenSlot = lapisInv + 2;
                mc.interactionManager.clickSlot(handler.syncId, screenSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                timer.reset();
                return;
            }
            return; // no lapis available
        }

        if (lapisSlot.getCount() < minLapis.get()) return;

        // Step 2: check enchant options are populated (level > 0 means option available)
        int level = enchantLevel.get() - 1; // 0-indexed
        if (handler.getEnchantmentPower(level) <= 0) {
            // Try lower levels
            for (int i = 2; i >= 0; i--) {
                if (handler.getEnchantmentPower(i) > 0) {
                    level = i;
                    break;
                }
            }
        }
        if (handler.getEnchantmentPower(level) <= 0) return;

        // Check player has enough levels
        if (mc.player.experienceLevel < handler.getEnchantmentPower(level)) return;

        // Click the enchantment button
        mc.interactionManager.clickButton(handler.syncId, level);
        timer.reset();
    }
}
