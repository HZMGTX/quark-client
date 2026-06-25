package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class InventorySorter extends Module {

    private final IntSetting sortKey = register(new IntSetting(
            "Sort Key", "GLFW key code to trigger sort (default: R)", GLFW.GLFW_KEY_R, 0, 400));
    private final BoolSetting autoSort = register(new BoolSetting(
            "Auto Sort", "Sort automatically every few seconds", false));
    private final IntSetting autoInterval = register(new IntSetting(
            "Auto Interval", "Seconds between auto-sorts", 10, 2, 60));

    private final TimerUtil autoTimer = new TimerUtil();
    private final TimerUtil sortTimer = new TimerUtil();
    private boolean needsSort = false;

    public InventorySorter() {
        super("InventorySorter", "Sorts inventory by category (Armor/Weapons/Food/Misc) on key press", Category.PLAYER);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (event.getKeyCode() == sortKey.get()) {
            needsSort = true;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        if (autoSort.isEnabled() && autoTimer.hasReached(autoInterval.get() * 1000L)) {
            needsSort = true;
            autoTimer.reset();
        }

        if (!needsSort) return;
        if (!sortTimer.hasReached(100)) return;
        needsSort = false;

        sortInventory();
    }

    /**
     * Sorts by category priority: Armor > Swords > Axes > Tools > Food > Blocks > Misc.
     * Uses shift-click to move items and relies on the game's own slot assignment.
     */
    private void sortInventory() {
        if (mc.player == null || mc.interactionManager == null) return;
        var inv = mc.player.getInventory();
        int syncId = mc.player.currentScreenHandler.syncId;

        // Collect all non-empty slot indices in main inventory (slots 9-35 in screen handler = inv 9-35)
        // and sort them by category weight
        int[] slots = new int[27];
        int count = 0;
        for (int i = 9; i < 36; i++) {
            if (!inv.getStack(i).isEmpty()) {
                slots[count++] = i;
            }
        }

        // Bubble-sort by category weight (simple, small N)
        for (int i = 0; i < count - 1; i++) {
            for (int j = 0; j < count - i - 1; j++) {
                int ia = slots[j], ib = slots[j + 1];
                if (weight(inv.getStack(ia)) > weight(inv.getStack(ib))) {
                    int tmp = slots[j]; slots[j] = slots[j + 1]; slots[j + 1] = tmp;
                }
            }
        }

        // Move items by shift-clicking them to re-stack/rearrange
        for (int i = 0; i < count; i++) {
            mc.interactionManager.clickSlot(syncId, slots[i], 0, SlotActionType.QUICK_MOVE, mc.player);
        }
    }

    private int weight(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ArmorItem)                    return 0;
        if (item instanceof SwordItem)                    return 1;
        if (item instanceof AxeItem)                      return 2;
        if (item instanceof PickaxeItem || item instanceof ShovelItem || item instanceof HoeItem) return 3;
        if (stack.contains(net.minecraft.component.DataComponentTypes.FOOD))                               return 4;
        if (item instanceof BlockItem)                    return 5;
        return 6;
    }
}
