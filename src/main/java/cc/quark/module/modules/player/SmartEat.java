package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
//? if mc >= "1.20.5" {
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
//?}
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class SmartEat extends Module {

    private final IntSetting hungerThreshold = register(new IntSetting(
            "HungerThreshold", "Eat when hunger drops below this level", 18, 1, 20));
    private final BoolSetting gapplePriority = register(new BoolSetting(
            "GapplePriority", "Eat golden apple first when low on health", false));
    private final IntSetting hpThreshold = register(new IntSetting(
            "HPThreshold", "Health (half-hearts) to trigger golden apple priority", 10, 1, 20));

    public SmartEat() {
        super("SmartEat", "Automatically eats the best available food based on hunger and health", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.isUsingItem()) return;

        int foodLevel = mc.player.getHungerManager().getFoodLevel();
        if (foodLevel >= hungerThreshold.get()) return;

        // Check gapple priority first
        if (gapplePriority.isEnabled() && mc.player.getHealth() < hpThreshold.get()) {
            int gappleSlot = findInHotbar(Items.GOLDEN_APPLE);
            if (gappleSlot != -1) {
                mc.player.getInventory().selectedSlot = gappleSlot;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                return;
            }
        }

        // Find best food by nutrition in hotbar
        int bestSlot = -1;
        float bestSaturation = -1f;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            float sat = getFoodSaturation(stack);
            if (sat < 0) continue;
            if (sat > bestSaturation) {
                bestSaturation = sat;
                bestSlot = i;
            }
        }

        if (bestSlot == -1) return;

        mc.player.getInventory().selectedSlot = bestSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }

    private int findInHotbar(net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        }
        return -1;
    }

    private float getFoodSaturation(ItemStack stack) {
        //? if mc >= "1.20.5" {
        if (!stack.contains(DataComponentTypes.FOOD)) return -1f;
        FoodComponent fc = stack.get(DataComponentTypes.FOOD);
        return fc != null ? fc.saturation() : -1f;
        //?} else {
        /*if (!stack.getItem().isFood()) return -1f;
        net.minecraft.item.FoodComponent fc = stack.getItem().getFoodComponent();
        return fc != null ? fc.getSaturationModifier() : -1f;*/
        //?}
    }
}
