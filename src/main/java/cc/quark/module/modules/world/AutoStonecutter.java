package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.*;
import net.minecraft.client.gui.screen.ingame.StonecutterScreen;
import net.minecraft.item.*;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoStonecutter extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between actions", 150, 50, 1000));
    private final ModeSetting preferredOutput = register(new ModeSetting(
            "PreferredOutput", "Preferred output type",
            "Slab", "Stairs", "Slab", "Wall"));

    private final TimerUtil timer = new TimerUtil();

    public AutoStonecutter() {
        super("AutoStonecutter", "Automatically operates the stonecutter to convert stone blocks", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.currentScreen instanceof StonecutterScreen)) return;
        if (!timer.hasReached(delay.get())) return;

        var handler = mc.player.currentScreenHandler;
        if (!(handler instanceof StonecutterScreenHandler cutterHandler)) return;

        // Stonecutter slots:
        // 0: input slot
        // 1: output slot
        // Player inventory starts at slot 2

        int containerSize = cutterHandler.slots.size();
        int invStart = 2;

        // Take result if available
        if (cutterHandler.slots.get(1).hasStack()) {
            mc.interactionManager.clickSlot(handler.syncId, 1, 0, SlotActionType.QUICK_MOVE, mc.player);
            timer.reset();
            return;
        }

        // Fill input slot if empty
        if (!cutterHandler.slots.get(0).hasStack()) {
            for (int i = invStart; i < containerSize; i++) {
                var slot = cutterHandler.slots.get(i);
                if (slot.hasStack() && isStoneType(slot.getStack().getItem())) {
                    mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    timer.reset();
                    return;
                }
            }
        }

        // Select recipe based on preferred output
        int recipeCount = cutterHandler.getAvailableRecipeCount();
        if (recipeCount > 0) {
            String pref = preferredOutput.get();
            int targetRecipe = 0; // Default to first recipe

            for (int i = 0; i < recipeCount; i++) {
                // Try to find recipe matching preference
                // Recipe selection by button index
                if (pref.equals("Stairs") && i == 0) { targetRecipe = i; break; }
                if (pref.equals("Slab") && i == 1) { targetRecipe = i; break; }
                if (pref.equals("Wall") && i == 2) { targetRecipe = i; break; }
            }

            mc.interactionManager.clickSlot(handler.syncId, targetRecipe, 1, SlotActionType.PICKUP, mc.player);
            timer.reset();
            return;
        }

        timer.reset();
    }

    private boolean isStoneType(Item item) {
        return item == Items.STONE
                || item == Items.COBBLESTONE
                || item == Items.STONE_BRICKS
                || item == Items.MOSSY_COBBLESTONE
                || item == Items.MOSSY_STONE_BRICKS
                || item == Items.SANDSTONE
                || item == Items.RED_SANDSTONE
                || item == Items.QUARTZ_BLOCK
                || item == Items.PURPUR_BLOCK
                || item == Items.BLACKSTONE
                || item == Items.POLISHED_BLACKSTONE
                || item == Items.DEEPSLATE
                || item == Items.COBBLED_DEEPSLATE;
    }
}
