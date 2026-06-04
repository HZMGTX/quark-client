package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

public class AutoEat2 extends Module {

    private final IntSetting threshold = register(new IntSetting(
            "Threshold", "Hunger level at or below which eating is triggered (0-20)", 14, 0, 20));

    private final StringSetting priority = register(new StringSetting(
            "Priority", "Comma-separated food item IDs in priority order", "bread,steak"));

    private int savedSlot = -1;
    private boolean eating = false;

    public AutoEat2() {
        super("AutoEat2", "Enhanced auto-eat with food priority list", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (eating && mc.options != null) {
            mc.options.useKey.setPressed(false);
            eating = false;
        }
        if (savedSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = savedSlot;
            savedSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        int hunger = mc.player.getHungerManager().getFoodLevel();
        if (hunger > threshold.get()) {
            if (eating) {
                mc.options.useKey.setPressed(false);
                eating = false;
                if (savedSlot != -1) {
                    mc.player.getInventory().selectedSlot = savedSlot;
                    savedSlot = -1;
                }
            }
            return;
        }

        if (mc.player.isUsingItem()) return;

        // Build priority list from setting
        List<String> priorities = Arrays.asList(priority.get().split(","));

        // Find best food slot in hotbar matching priority
        int bestSlot = -1;
        int bestPrio = Integer.MAX_VALUE;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            String id = Registries.ITEM.getId(stack.getItem()).getPath();

            // Check if it's edible
            boolean isFood = stack.contains(net.minecraft.component.DataComponentTypes.FOOD);
            if (!isFood) continue;

            int prio = priorities.indexOf(id);
            if (prio == -1) prio = priorities.size(); // deprioritize unlisted foods

            if (prio < bestPrio) {
                bestPrio = prio;
                bestSlot = i;
            }
        }

        if (bestSlot == -1) return;

        if (savedSlot == -1) savedSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = bestSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.options.useKey.setPressed(true);
        eating = true;
    }
}
