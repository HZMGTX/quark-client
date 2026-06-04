package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashSet;
import java.util.Set;

public class ChestCleaner extends Module {

    private final BoolSetting confirm = register(new BoolSetting(
            "Confirm", "Print confirmation to chat when duplicates removed", true));

    private final TimerUtil timer = new TimerUtil();

    public ChestCleaner() {
        super("ChestCleaner", "Removes duplicate items from open chests", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler handler)) return;
        if (!timer.hasReached(200)) return;
        timer.reset();

        int rows = handler.getRows();
        int chestSlots = rows * 9;

        Set<Item> seen = new HashSet<>();

        for (int i = 0; i < chestSlots; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();

            if (!seen.add(item)) {
                // Duplicate — shift-click out of chest
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                if (confirm.isEnabled()) {
                    cc.quark.util.ChatUtil.info("ChestCleaner: Removed duplicate " + stack.getName().getString());
                }
                return;
            }
        }
    }
}
