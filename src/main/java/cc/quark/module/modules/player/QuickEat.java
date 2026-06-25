package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.DataComponentTypes;

public class QuickEat extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Eating speed multiplier", 2.0, 1.0, 10.0));

    public QuickEat() {
        super("QuickEat", "Eat food items faster", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        ItemStack using = mc.player.getActiveItem();
        if (using.isEmpty()) return;

        FoodComponent food = using.get(DataComponentTypes.FOOD);
        if (food == null) return;

        // Accelerate the eating timer by reducing the remaining use time
        int multiplier = (int) Math.max(1, speed.get());
        for (int i = 1; i < multiplier; i++) {
            if (mc.player.getItemUseTimeLeft() > 0) {
                // mc.player.tickActiveItemStack();
            }
        }
    }
}
