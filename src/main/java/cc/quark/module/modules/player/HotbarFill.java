package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;

public class HotbarFill extends Module {

    private final BoolSetting weapons = register(new BoolSetting(
            "Weapons", "Fill hotbar with weapons from inventory", true));
    private final BoolSetting food = register(new BoolSetting(
            "Food", "Fill hotbar with food from inventory", true));
    private final BoolSetting blocks = register(new BoolSetting(
            "Blocks", "Fill hotbar with blocks from inventory", false));

    private final TimerUtil timer = new TimerUtil();

    public HotbarFill() {
        super("HotbarFill", "Fills hotbar from inventory intelligently", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!timer.hasReached(800)) return;
        timer.reset();

        for (int hotSlot = 0; hotSlot < 9; hotSlot++) {
            ItemStack current = mc.player.getInventory().getStack(hotSlot);
            if (!current.isEmpty()) continue;

            int srcSlot = findReplacement();
            if (srcSlot == -1) continue;

            int syncId = mc.player.playerScreenHandler.syncId;
            int guiHot = 36 + hotSlot;
            int guiSrc = srcSlot;

            mc.interactionManager.clickSlot(syncId, guiSrc, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(syncId, guiHot, 0, SlotActionType.PICKUP, mc.player);
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                mc.interactionManager.clickSlot(syncId, guiSrc, 0, SlotActionType.PICKUP, mc.player);
            }
            return;
        }
    }

    private int findReplacement() {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (weapons.isEnabled() && isWeapon(stack)) return i;
            if (food.isEnabled() && isFood(stack)) return i;
            if (blocks.isEnabled() && stack.getItem() instanceof BlockItem) return i;
        }
        return -1;
    }

    private boolean isWeapon(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof SwordItem || item instanceof AxeItem || item instanceof BowItem
                || item instanceof CrossbowItem || item instanceof TridentItem;
    }

    private boolean isFood(ItemStack stack) {
        return stack.get(DataComponentTypes.FOOD) != null;
    }
}
