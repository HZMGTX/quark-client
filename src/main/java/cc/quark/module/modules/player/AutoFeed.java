package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoFeed extends Module {

    private final IntSetting hungerLevel = register(new IntSetting(
            "Hunger Level", "Eat food when hunger drops below this level", 16, 1, 18));

    private final TimerUtil timer = new TimerUtil();

    public AutoFeed() {
        super("AutoFeed", "Auto-eats food from the hotbar when hunger drops below threshold", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!timer.hasReached(200)) return;

        int food = mc.player.getHungerManager().getFoodLevel();
        if (food >= hungerLevel.get()) return;

        int foodSlot = findFoodSlot();
        if (foodSlot == -1) return;

        timer.reset();

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = foodSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prev;
    }

    private int findFoodSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.contains(DataComponentTypes.FOOD)) {
                FoodComponent fc = stack.get(DataComponentTypes.FOOD);
                if (fc != null && fc.nutrition() > 0) return i;
            }
        }
        return -1;
    }
}
