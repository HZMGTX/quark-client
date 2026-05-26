package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * QuickDrop - automatically drops worthless junk items from the hotbar.
 */
public class QuickDrop extends Module {

    private final BoolSetting rottenFlesh = register(new BoolSetting("RottenFlesh", "Drop rotten flesh", true));
    private final BoolSetting cobblestone = register(new BoolSetting("Cobblestone", "Drop cobblestone", false));

    public QuickDrop() {
        super("QuickDrop", "Drops junk items from the hotbar", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            boolean junk = (rottenFlesh.isEnabled() && stack.isOf(Items.ROTTEN_FLESH))
                    || (cobblestone.isEnabled() && stack.isOf(Items.COBBLESTONE));
            if (junk) {
                mc.player.getInventory().selectedSlot = i;
                mc.player.dropSelectedItem(true);
            }
        }
    }
}
