package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

/**
 * TotemSwap - instantly swaps a totem from inventory to offhand when incoming
 * damage would be lethal (damage >= currentHP + absorption - buffer).
 */
public class TotemSwap extends Module {

    private static final int OFFHAND_SLOT = 45;

    private final DoubleSetting buffer = register(new DoubleSetting(
            "Buffer HP", "Swap when damage would reduce total HP below this buffer", 1.0, 0.5, 4.0));

    private final BoolSetting notifySwap = register(new BoolSetting(
            "Notify", "Send a chat notification when a swap is performed", false));

    public TotemSwap() {
        super("TotemSwap", "Instantly swaps totem to offhand when taking lethal damage", Category.COMBAT);
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null || mc.interactionManager == null) return;

        float currentHp   = mc.player.getHealth();
        float absorption  = mc.player.getAbsorptionAmount();
        float totalHp     = currentHp + absorption;
        float damage      = event.getAmount();

        // Only swap if damage would be near-lethal
        if (damage < totalHp - (float) buffer.get()) return;

        // Already have a totem in offhand — nothing to do
        if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;

        // Find a totem in inventory
        int totemSlot = findTotemSlot();
        if (totemSlot == -1) return;

        int syncId = mc.player.playerScreenHandler.syncId;

        // Pick up totem from its slot
        mc.interactionManager.clickSlot(syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
        // Place it into the offhand slot
        mc.interactionManager.clickSlot(syncId, OFFHAND_SLOT, 0, SlotActionType.PICKUP, mc.player);
        // If a previous offhand item is now on the cursor, put it back
        if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            mc.interactionManager.clickSlot(syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
        }

        if (notifySwap.isEnabled()) {
            mc.player.sendMessage(Text.literal(
                    "[TotemSwap] Swapped totem (HP: " + String.format("%.1f", currentHp) + ")"), false);
        }
    }

    private int findTotemSlot() {
        for (int i = 0; i <= 35; i++) {
            var stack = mc.player.playerScreenHandler.getSlot(i).getStack();
            if (stack.isOf(Items.TOTEM_OF_UNDYING)) return i;
        }
        return -1;
    }
}
