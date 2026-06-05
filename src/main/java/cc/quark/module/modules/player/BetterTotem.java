package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * BetterTotem - Enhances totem-of-undying management:
 *  - Keeps the best totem in the offhand at all times.
 *  - Detects when a totem pops (offhand becomes empty after nearly dying) and
 *    immediately replaces it from inventory.
 *  - Optionally alerts via chat when a totem pops or when totems are running low.
 */
public class BetterTotem extends Module {

    private final DoubleSetting lowAlert = register(new DoubleSetting(
            "Low Alert", "Warn in chat when totem count drops to this value", 3.0, 1.0, 16.0));
    private final BoolSetting popAlert = register(new BoolSetting(
            "Pop Alert", "Notify when a totem pops", true));
    private final BoolSetting lowAlertEnabled = register(new BoolSetting(
            "Low Alert Enabled", "Warn when totems are running low", true));
    private final BoolSetting autoReplace = register(new BoolSetting(
            "Auto Replace", "Instantly swap a new totem into offhand after a pop", true));
    private final BoolSetting preferOffhand = register(new BoolSetting(
            "Keep Offhand", "Always keep a totem in offhand, not just after a pop", true));

    private final TimerUtil alertCooldown = new TimerUtil();
    private boolean hadTotemLastTick = false;
    private int lastTotemCount = -1;

    public BetterTotem() {
        super("BetterTotem", "Enhances totem behaviour: auto-replace after pop, low-count alerts", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        hadTotemLastTick = false;
        lastTotemCount = -1;
        alertCooldown.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        ItemStack offhand = mc.player.getOffHandStack();
        boolean hasTotemNow = offhand.getItem() == Items.TOTEM_OF_UNDYING;

        // Detect pop: had totem last tick, lost it this tick
        if (hadTotemLastTick && !hasTotemNow) {
            if (popAlert.isEnabled() && alertCooldown.hasReached(1000)) {
                ChatUtil.warn("Totem popped!");
                alertCooldown.reset();
            }
            if (autoReplace.isEnabled()) {
                swapTotemToOffhand();
            }
        } else if (!hasTotemNow && preferOffhand.isEnabled()) {
            // Proactively keep totem in offhand
            swapTotemToOffhand();
        }

        hadTotemLastTick = mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;

        // Low-count alert
        if (lowAlertEnabled.isEnabled()) {
            int count = countTotems();
            if (count != lastTotemCount) {
                lastTotemCount = count;
                if (count > 0 && count <= (int) lowAlert.get() && alertCooldown.hasReached(5000)) {
                    ChatUtil.warn("Low totems: " + count + " remaining!");
                    alertCooldown.reset();
                }
            }
        }
    }

    /** Moves a totem from the main inventory into the offhand slot (slot 45). */
    private void swapTotemToOffhand() {
        if (mc.player == null || mc.interactionManager == null) return;

        // Search hotbar first, then main inventory
        int totemSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }
        if (totemSlot == -1) {
            for (int i = 9; i < 36; i++) {
                if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                    totemSlot = i;
                    break;
                }
            }
        }
        if (totemSlot == -1) return;

        int syncId = mc.player.currentScreenHandler.syncId;
        // Convert inventory slot index to screen handler slot index
        int screenSlot = totemSlot < 9 ? totemSlot + 36 : totemSlot;

        // Pick up the totem
        mc.interactionManager.clickSlot(syncId, screenSlot, 0, SlotActionType.PICKUP, mc.player);
        // Place in offhand (screen slot 45)
        mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);
        // If something was displaced back onto cursor, return it to the original slot
        ItemStack cursor = mc.player.currentScreenHandler.getCursorStack();
        if (!cursor.isEmpty()) {
            mc.interactionManager.clickSlot(syncId, screenSlot, 0, SlotActionType.PICKUP, mc.player);
        }
    }

    /** Counts all totem-of-undying items across the entire inventory (including offhand). */
    private int countTotems() {
        if (mc.player == null) return 0;
        int count = 0;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) count++;
        }
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) count++;
        return count;
    }

    @Override
    public String getSuffix() {
        if (mc.player == null) return "";
        int c = countTotems();
        return c > 0 ? c + "x" : "none";
    }
}
