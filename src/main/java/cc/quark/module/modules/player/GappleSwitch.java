package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class GappleSwitch extends Module {

    private final ModeSetting type = register(new ModeSetting(
            "Type", "Which golden apple type to switch to", "Normal",
            "Normal", "Enchanted"));

    private final IntSetting hotbarSlot = register(new IntSetting(
            "Hotbar Slot", "Preferred hotbar slot to place the golden apple (1-9)", 8, 1, 9));

    private int savedSlot = -1;

    public GappleSwitch() {
        super("GappleSwitch", "Quick-switches to golden apple slot", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (savedSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = savedSlot;
            savedSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean wantEnchanted = type.is("Enchanted");

        // Search hotbar for matching gapple
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            boolean match = wantEnchanted
                    ? stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE
                    : stack.getItem() == Items.GOLDEN_APPLE;

            if (match) {
                int target = Math.min(8, Math.max(0, hotbarSlot.get() - 1));
                if (mc.player.getInventory().selectedSlot != i) {
                    if (savedSlot == -1) savedSlot = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = i;
                }
                return;
            }
        }

        // Not found — restore previous slot
        if (savedSlot != -1) {
            mc.player.getInventory().selectedSlot = savedSlot;
            savedSlot = -1;
        }
    }
}
