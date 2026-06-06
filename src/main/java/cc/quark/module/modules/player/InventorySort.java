package cc.quark.module.modules.player;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.setting.BoolSetting;
import cc.quark.module.setting.IntSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class InventorySort extends Module {

    private final BoolSetting autoSort = new BoolSetting("AutoSort", false);
    private final IntSetting interval = new IntSetting("Interval", 20, 5, 100);

    private int timer = 0;

    public InventorySort() {
        super("InventorySort", "Keeps inventory sorted by item category and stackability", Category.PLAYER);
        addSettings(autoSort, interval);
    }

    @Override public void onEnable()  { Quark.mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { Quark.mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick event) {
        if (!autoSort.isEnabled()) return;
        var mc = Quark.mc;
        if (mc == null || mc.player == null) return;
        if (mc.currentScreen != null) return;

        if (++timer < interval.get()) return;
        timer = 0;

        // Simple: consolidate identical stacks (move smaller stacks onto larger ones)
        var inv = mc.player.getInventory();
        for (int i = 9; i < 36; i++) {
            ItemStack si = inv.getStack(i);
            if (si.isEmpty() || si.getCount() >= si.getMaxCount()) continue;
            for (int j = 9; j < 36; j++) {
                if (i == j) continue;
                ItemStack sj = inv.getStack(j);
                if (!ItemStack.areItemsEqual(si, sj)) continue;
                if (sj.getCount() < si.getCount()) continue;
                // Shift-click to consolidate
                mc.interactionManager.clickSlot(
                    mc.player.playerScreenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                break;
            }
        }
    }
}
