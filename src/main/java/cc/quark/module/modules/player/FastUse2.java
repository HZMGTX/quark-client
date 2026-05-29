package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.item.FoodItem;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;

import java.lang.reflect.Field;

public class FastUse2 extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Which items to fast-use",
            "All", "All", "Food", "Items"));

    private Field itemUseCooldownField = null;

    public FastUse2() {
        super("FastUse2", "Reduces item use / eating time by zeroing use-tick counter", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        Item heldItem = mc.player.getMainHandStack().getItem();
        boolean isFood = mc.player.getMainHandStack().isFood();

        boolean apply = switch (mode.get()) {
            case "Food"  -> isFood;
            case "Items" -> !isFood;
            default      -> true; // "All"
        };

        if (!apply) return;

        // Zero the item use cooldown (itemUseTimeLeft field)
        try {
            if (itemUseCooldownField == null) {
                itemUseCooldownField = findField(mc.player.getClass(),
                        "itemUseTimeLeft", "field_7483", "age");
            }
            if (itemUseCooldownField != null) {
                itemUseCooldownField.setAccessible(true);
                int current = (int) itemUseCooldownField.get(mc.player);
                if (current > 0) {
                    itemUseCooldownField.set(mc.player, Math.max(0, current - 2));
                }
            }
        } catch (Exception ignored) {}

        // Also fire extra interactItem to advance use state
        if (mc.player.isUsingItem()) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }

    private static Field findField(Class<?> clazz, String... names) {
        for (String name : names) {
            Class<?> c = clazz;
            while (c != null) {
                try { return c.getDeclaredField(name); } catch (NoSuchFieldException e) {}
                c = c.getSuperclass();
            }
        }
        return null;
    }
}
