package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

/**
 * AutoArmorEquip - shift-clicks any armor found in the inventory so the game equips it.
 */
public class AutoArmorEquip extends Module {

    private final BoolSetting notify = register(new BoolSetting("Notify", "Print to chat when equipping", false));

    public AutoArmorEquip() {
        super("AutoArmorEquip", "Automatically equips armor from inventory", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof ArmorItem)) continue;
            // Shift-click the armor; vanilla routes it to the correct armor slot if empty.
            int containerSlot = i < 9 ? 36 + i : i;
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                    containerSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
        }
    }
}
