package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * EatDelay - Reduces eating animation delay so food is consumed faster.
 * Works by rapidly re-triggering the use-item action each tick while eating.
 */
public class EatDelay extends Module {

    private final IntSetting speedTicks = register(new IntSetting(
            "Speed", "Ticks to shorten eating by (higher = faster consume)", 20, 1, 31));

    private final BoolSetting onlyWhenHungry = register(new BoolSetting(
            "Only When Hungry", "Only activate when hunger is not full", true));

    private final BoolSetting mainHandOnly = register(new BoolSetting(
            "Main Hand Only", "Only apply to main-hand food items", false));

    public EatDelay() {
        super("EatDelay", "Reduces eating animation delay for faster food consumption", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (onlyWhenHungry.isEnabled()
                && mc.player.getHungerManager().getFoodLevel() >= 20) return;

        ItemStack mainHand   = mc.player.getMainHandStack();
        ItemStack offHand    = mc.player.getOffHandStack();

        boolean mainIsFood   = isFood(mainHand);
        boolean offIsFood    = !mainHandOnly.isEnabled() && isFood(offHand);

        if (!mainIsFood && !offIsFood) return;

        // If the player is currently eating, shorten the remaining item-use ticks
        if (mc.player.isUsingItem()) {
            int remaining = mc.player.getItemUseTimeLeft();
            // Jump the use-tick counter forward by speedTicks to finish eating sooner
            for (int i = 0; i < speedTicks.get() && mc.player.isUsingItem(); i++) {
                mc.player.tickHandSwing();
            }
        } else {
            // Not currently eating - start eating if food is in hand
            Hand hand = mainIsFood ? Hand.MAIN_HAND : Hand.OFF_HAND;
            mc.interactionManager.interactItem(mc.player, hand);
        }
    }

    private boolean isFood(ItemStack stack) {
        if (stack.isEmpty()) return false;
        FoodComponent fc = stack.getItem().getFoodComponent();
        return fc != null;
    }
}
