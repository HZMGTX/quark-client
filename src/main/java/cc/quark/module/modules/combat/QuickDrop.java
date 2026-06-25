package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class QuickDrop extends Module {

    private final IntSetting slot = register(new IntSetting("Slot", "Hotbar slot to drop (0-8)", 0, 0, 8));

    private final TimerUtil timer = new TimerUtil();

    public QuickDrop() {
        super("QuickDrop", "Quickly drops entire hotbar slot contents", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!timer.hasReached(100)) return;

        int targetSlot = Math.max(0, Math.min(8, slot.get()));
        ItemStack stack = mc.player.getInventory().getStack(targetSlot);
        if (stack.isEmpty()) {
            // Nothing to drop, disable self
            this.toggle();
            return;
        }

        // Drop entire stack from inventory slot
        // Hotbar slots in the player's screen handler are 36 + hotbarSlot
        int screenSlot = 36 + targetSlot;
        mc.interactionManager.clickSlot(
                mc.player.playerScreenHandler.syncId,
                screenSlot,
                1, // button 1 = drop whole stack (Ctrl+Q equivalent)
                SlotActionType.THROW,
                mc.player
        );

        timer.reset();
    }
}
