package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;

import java.util.ArrayList;
import java.util.List;

public class CraftHelper extends Module {

    private final BoolSetting auto = register(new BoolSetting(
            "Auto", "Automatically open crafting table for craftable items", false));

    private final TimerUtil timer = new TimerUtil();
    private final List<String> lastNotified = new ArrayList<>();

    public CraftHelper() {
        super("CraftHelper", "Shows crafting recipes for needed items", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
        lastNotified.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(3000)) return;
        timer.reset();

        if (mc.world.getRecipeManager() == null) return;

        List<String> craftable = new ArrayList<>();

        for (var recipeEntry : mc.world.getRecipeManager().listAllOfType(RecipeType.CRAFTING)) {
            CraftingRecipe recipe = recipeEntry.value();
            ItemStack output = recipe.getResult(mc.world.getRegistryManager());
            if (output.isEmpty()) continue;

            String name = output.getName().getString();
            if (lastNotified.contains(name)) continue;

            if (canCraftRecipe(recipe)) {
                craftable.add(name);
                lastNotified.add(name);
            }
        }

        if (!craftable.isEmpty()) {
            ChatUtil.info("CraftHelper: Can craft: " + String.join(", ", craftable));
        }
    }

    private boolean canCraftRecipe(CraftingRecipe recipe) {
        for (var ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) continue;
            boolean found = false;
            for (int i = 0; i < 36; i++) {
                ItemStack inv = mc.player.getInventory().getStack(i);
                if (!inv.isEmpty() && ingredient.test(inv)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }
}
