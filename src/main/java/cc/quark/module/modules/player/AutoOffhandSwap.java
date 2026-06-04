package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoOffhandSwap extends Module {

    private final ModeSetting item = register(new ModeSetting(
            "Item", "Item type to keep in offhand",
            "Totem", "Totem", "Shield", "Crystal", "Arrow"));

    private final TimerUtil timer = new TimerUtil();
    private static final int OFFHAND_SLOT = 45;

    public AutoOffhandSwap() {
        super("AutoOffhandSwap", "Automatically puts items in offhand based on mode", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!timer.hasReached(400)) return;
        timer.reset();

        Item desired = getDesiredItem();
        if (desired == null) return;

        ItemStack offhand = mc.player.getOffHandStack();
        if (!offhand.isEmpty() && offhand.isOf(desired)) return;

        int srcSlot = findInInventory(desired);
        if (srcSlot == -1) return;

        int syncId = mc.player.playerScreenHandler.syncId;
        mc.interactionManager.clickSlot(syncId, srcSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, OFFHAND_SLOT, 0, SlotActionType.PICKUP, mc.player);
        if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            mc.interactionManager.clickSlot(syncId, srcSlot, 0, SlotActionType.PICKUP, mc.player);
        }
    }

    private Item getDesiredItem() {
        return switch (item.get()) {
            case "Totem"  -> Items.TOTEM_OF_UNDYING;
            case "Shield" -> Items.SHIELD;
            case "Crystal" -> Items.END_CRYSTAL;
            case "Arrow"  -> Items.ARROW;
            default       -> null;
        };
    }

    private int findInInventory(Item target) {
        for (int i = 0; i <= 35; i++) {
            ItemStack stack = mc.player.playerScreenHandler.getSlot(i).getStack();
            if (stack.isOf(target)) return i;
        }
        return -1;
    }
}
