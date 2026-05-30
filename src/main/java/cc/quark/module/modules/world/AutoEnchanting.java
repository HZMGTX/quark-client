package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoEnchanting extends Module {

    private final IntSetting level = register(new IntSetting(
            "Level", "Which enchant level slot to click (1-3)", 3, 1, 3));

    private final TimerUtil timer = new TimerUtil();

    public AutoEnchanting() {
        super("AutoEnchanting", "Automatically clicks the configured enchantment level when an enchanting table is open", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.currentScreen instanceof EnchantmentScreen)) return;
        if (!timer.hasReached(500)) return;

        var handler = mc.player.currentScreenHandler;
        if (!(handler instanceof EnchantmentScreenHandler enchHandler)) return;

        int slotIndex = level.get() - 1; // 0-indexed: slot 0 = level 1, slot 2 = level 3

        // Check if the enchantment level is available (power > 0)
        int enchPower = enchHandler.enchantmentPower[slotIndex];
        if (enchPower <= 0) return;

        // Slot 0 is the item to enchant, slot 1 is lapis
        // Enchantment buttons are accessed via button clicks (0, 1, or 2)
        mc.interactionManager.clickSlot(handler.syncId, slotIndex, 0, SlotActionType.PICKUP, mc.player);
        timer.reset();
    }
}
