package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoOffhand extends Module {

    private final ModeSetting priority = register(new ModeSetting(
            "Priority", "Preferred offhand item",
            "Totem", "Totem", "Shield", "Gapple"));
    private final BoolSetting fillTotem  = register(new BoolSetting("Totem",  "Move totems to offhand", true));
    private final BoolSetting fillShield = register(new BoolSetting("Shield", "Move shield to offhand if no totem", true));
    private final BoolSetting fillGapple = register(new BoolSetting("Gapple", "Move golden apple to offhand as fallback", false));

    private final TimerUtil timer = new TimerUtil();

    private static final int OFFHAND_SLOT = 45;

    public AutoOffhand() {
        super("AutoOffhand", "Keeps offhand filled with a totem, shield, or gapple from inventory", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!timer.hasReached(500)) return;
        timer.reset();

        ItemStack offhand = mc.player.getOffHandStack();
        Item desired = getDesiredItem(offhand);
        if (desired == null) return;

        if (!offhand.isEmpty() && offhand.isOf(desired)) return;

        int srcSlot = findSlot(desired);
        if (srcSlot == -1) return;

        int syncId = mc.player.playerScreenHandler.syncId;

        mc.interactionManager.clickSlot(syncId, srcSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, OFFHAND_SLOT, 0, SlotActionType.PICKUP, mc.player);

        if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            mc.interactionManager.clickSlot(syncId, srcSlot, 0, SlotActionType.PICKUP, mc.player);
        }
    }

    private Item getDesiredItem(ItemStack offhand) {
        if (priority.is("Totem")) {
            if (fillTotem.isEnabled()  && hasItem(Items.TOTEM_OF_UNDYING)) return Items.TOTEM_OF_UNDYING;
            if (fillShield.isEnabled() && hasItem(Items.SHIELD))           return Items.SHIELD;
            if (fillGapple.isEnabled() && hasItem(Items.GOLDEN_APPLE))     return Items.GOLDEN_APPLE;
        } else if (priority.is("Shield")) {
            if (fillShield.isEnabled() && hasItem(Items.SHIELD))           return Items.SHIELD;
            if (fillTotem.isEnabled()  && hasItem(Items.TOTEM_OF_UNDYING)) return Items.TOTEM_OF_UNDYING;
            if (fillGapple.isEnabled() && hasItem(Items.GOLDEN_APPLE))     return Items.GOLDEN_APPLE;
        } else if (priority.is("Gapple")) {
            if (fillGapple.isEnabled() && hasItem(Items.GOLDEN_APPLE))     return Items.GOLDEN_APPLE;
            if (fillTotem.isEnabled()  && hasItem(Items.TOTEM_OF_UNDYING)) return Items.TOTEM_OF_UNDYING;
            if (fillShield.isEnabled() && hasItem(Items.SHIELD))           return Items.SHIELD;
        }
        return null;
    }

    private boolean hasItem(Item item) {
        return findSlot(item) != -1;
    }

    private int findSlot(Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i <= 35; i++) {
            if (mc.player.playerScreenHandler.getSlot(i).getStack().isOf(item)) return i;
        }
        return -1;
    }
}
