package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.InventoryUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class SaturationKeeper extends Module {

    private final DoubleSetting minSaturation = register(new DoubleSetting(
            "Min Saturation", "Eat when saturation drops below this level", 3.0, 0.0, 20.0));
    private final BoolSetting autoEat = register(new BoolSetting(
            "Auto Eat", "Automatically eat best food to restore saturation", true));

    public SaturationKeeper() {
        super("SaturationKeeper", "Maintains high saturation by auto-eating high-saturation food", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        var hungerManager = mc.player.getHungerManager();
        float saturation = hungerManager.getSaturationLevel();
        int food = hungerManager.getFoodLevel();

        if (saturation >= minSaturation.get()) return;
        if (!autoEat.isEnabled()) return;

        // Find and eat best food
        ItemStack best = InventoryUtil.getBestFood();
        if (best.isEmpty()) return;

        // Find the slot of best food in hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.isOf(best.getItem())) {
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                return;
            }
        }
    }
}
