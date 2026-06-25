package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class FoodSwapper extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting("Threshold", "Hunger level to trigger swap", 15.0, 5.0, 20.0));
    private final ModeSetting prefer = register(new ModeSetting("Prefer", "Preferred food type", "Any", "Golden Apple", "Steak", "Any"));

    public FoodSwapper() {
        super("FoodSwapper", "Swaps to best food when hungry", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.getHungerManager().getFoodLevel() >= threshold.getValue()) return;

        int bestSlot = -1;
        int bestFood = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            FoodComponent food = stack.getItem().getFoodComponent();
            if (food == null) continue;
            int hunger = food.getHunger();
            if (hunger > bestFood) {
                bestFood = hunger;
                bestSlot = i;
            }
        }

        if (bestSlot >= 0 && mc.player.getInventory().selectedSlot != bestSlot) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }
}
