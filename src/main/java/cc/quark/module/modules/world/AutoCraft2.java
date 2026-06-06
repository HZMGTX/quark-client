package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

/**
 * AutoCraft2 — automatically crafts items using a configured recipe list
 * when a crafting table is open.
 */
public class AutoCraft2 extends Module {

    private final StringSetting targetItem = register(new StringSetting(
            "Target", "Item ID to craft (e.g. stick, crafting_table)", "stick"));
    private final IntSetting amount = register(new IntSetting(
            "Amount", "How many times to craft (0 = unlimited)", 0, 0, 64));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between each craft action", 200, 50, 2000));
    private final BoolSetting shiftClick = register(new BoolSetting(
            "Shift Click", "Shift-click to take entire stack from result slot", true));

    private final TimerUtil timer = new TimerUtil();
    private int craftCount = 0;

    public AutoCraft2() {
        super("AutoCraft2", "Automatically crafts items using recipes from a list", Category.WORLD);
    }

    @Override
    public void onEnable() {
        craftCount = 0;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!(mc.currentScreen instanceof CraftingScreen)) return;
        if (!timer.hasReached(delay.get())) return;

        if (amount.get() > 0 && craftCount >= amount.get()) return;

        CraftingScreenHandler handler = (CraftingScreenHandler) mc.player.currentScreenHandler;

        // Slot 0 is the result slot in a crafting screen handler
        var resultStack = handler.getSlot(0).getStack();
        if (resultStack.isEmpty()) return;

        // Check if result matches target
        String target = targetItem.get().trim().toLowerCase();
        String resultId = net.minecraft.registry.Registries.ITEM.getId(resultStack.getItem()).getPath();
        if (!target.isEmpty() && !resultId.contains(target)) return;

        // Click the result slot to take the crafted item
        SlotActionType action = shiftClick.isEnabled() ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP;
        mc.interactionManager.clickSlot(handler.syncId, 0, 0, action, mc.player);

        craftCount++;
        timer.reset();
    }
}
