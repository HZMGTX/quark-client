package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.ingame.BrewingStandScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoBrew extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between actions", 500, 100, 2000));

    private final TimerUtil timer = new TimerUtil();

    public AutoBrew() {
        super("AutoBrew", "Automatically operates the brewing stand to brew potions", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.currentScreen instanceof BrewingStandScreen)) return;
        if (!timer.hasReached(delay.get())) return;

        var handler = mc.player.currentScreenHandler;
        if (!(handler instanceof BrewingStandScreenHandler brewHandler)) return;

        // Brewing stand slots:
        // 0-2: potion output slots (bottom)
        // 3: ingredient slot (top)
        // 4: fuel (blaze powder)

        int containerSize = brewHandler.slots.size();
        int invStart = 5; // player inventory starts at slot 5

        // Take finished potions from slots 0-2
        for (int i = 0; i < 3; i++) {
            var slot = brewHandler.slots.get(i);
            if (slot.hasStack()) {
                var item = slot.getStack().getItem();
                // Check if it's a finished potion (not a water bottle input waiting to brew)
                if (item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION) {
                    mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    timer.reset();
                    return;
                }
            }
        }

        // Add fuel if empty
        var fuelSlot = brewHandler.slots.get(4);
        if (!fuelSlot.hasStack()) {
            for (int i = invStart; i < containerSize; i++) {
                var invSlot = brewHandler.slots.get(i);
                if (invSlot.hasStack() && invSlot.getStack().getItem() == Items.BLAZE_POWDER) {
                    mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    timer.reset();
                    return;
                }
            }
        }

        // Add ingredient if empty
        var ingSlot = brewHandler.slots.get(3);
        if (!ingSlot.hasStack()) {
            for (int i = invStart; i < containerSize; i++) {
                var invSlot = brewHandler.slots.get(i);
                if (invSlot.hasStack() && isIngredient(invSlot.getStack().getItem())) {
                    mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    timer.reset();
                    return;
                }
            }
        }

        // Fill empty potion slots with water bottles
        for (int i = 0; i < 3; i++) {
            if (!brewHandler.slots.get(i).hasStack()) {
                for (int j = invStart; j < containerSize; j++) {
                    var invSlot = brewHandler.slots.get(j);
                    if (invSlot.hasStack() && invSlot.getStack().getItem() == Items.GLASS_BOTTLE) {
                        mc.interactionManager.clickSlot(handler.syncId, j, 0, SlotActionType.QUICK_MOVE, mc.player);
                        timer.reset();
                        return;
                    }
                }
            }
        }

        timer.reset();
    }

    private boolean isIngredient(net.minecraft.item.Item item) {
        return item == Items.NETHER_WART
                || item == Items.BLAZE_POWDER
                || item == Items.GLOWSTONE_DUST
                || item == Items.REDSTONE
                || item == Items.GUNPOWDER
                || item == Items.DRAGON_BREATH
                || item == Items.FERMENTED_SPIDER_EYE
                || item == Items.SUGAR
                || item == Items.SPIDER_EYE
                || item == Items.MAGMA_CREAM
                || item == Items.GOLDEN_CARROT
                || item == Items.GLISTERING_MELON_SLICE
                || item == Items.RABBIT_FOOT
                || item == Items.PUFFERFISH
                || item == Items.TURTLE_SCUTE
                || item == Items.PHANTOM_MEMBRANE;
    }
}
