package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

/**
 * ChestStealer - automatically transfers all items from an open chest to the player's inventory.
 */
public class ChestStealer extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between each item steal", 2, 1, 10));

    private final BoolSetting ignoreJunk = register(new BoolSetting(
            "Ignore Junk", "Skip rotten flesh, seeds, and other junk items", true));

    private int ticksSinceLastSteal = 0;

    public ChestStealer() {
        super("ChestStealer", "Auto-steals items from open chests", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // Only active when a chest screen is open
        if (!(mc.currentScreen instanceof GenericContainerScreen)
                && !(mc.currentScreen instanceof ShulkerBoxScreen)) return;

        ticksSinceLastSteal++;
        if (ticksSinceLastSteal < delay.get()) return;
        ticksSinceLastSteal = 0;

        GenericContainerScreenHandler handler = (GenericContainerScreenHandler) mc.player.currentScreenHandler;
        List<Slot> slots = handler.slots;

        // Container slots are the first N slots (before the player inventory)
        int containerSize = handler.getRows() * 9;

        for (int i = 0; i < containerSize; i++) {
            Slot slot = slots.get(i);
            if (!slot.hasStack()) continue;

            ItemStack stack = slot.getStack();

            if (ignoreJunk.isEnabled() && isJunk(stack)) continue;

            // Shift-click to move item to player inventory
            mc.interactionManager.clickSlot(
                    handler.syncId,
                    i,
                    0,
                    SlotActionType.QUICK_MOVE,
                    mc.player
            );
            return; // One item per tick-delay cycle
        }
    }

    private boolean isJunk(ItemStack stack) {
        return stack.getItem() == Items.ROTTEN_FLESH
                || stack.getItem() == Items.WHEAT_SEEDS
                || stack.getItem() == Items.BEETROOT_SEEDS
                || stack.getItem() == Items.MELON_SEEDS
                || stack.getItem() == Items.PUMPKIN_SEEDS
                || stack.getItem() == Items.POISONOUS_POTATO
                || stack.getItem() == Items.SPIDER_EYE
                || stack.getItem() == Items.STRING
                || stack.getItem() == Items.BONE
                || stack.getItem() == Items.ARROW;
    }
}
