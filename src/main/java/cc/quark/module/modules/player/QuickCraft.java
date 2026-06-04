package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.PlayerScreenHandler;

public class QuickCraft extends Module {

    private final ModeSetting recipe = register(new ModeSetting(
            "Recipe", "Preset recipe to auto-craft", "Planks", "Planks", "Sticks", "Torches"));

    private final IntSetting count = register(new IntSetting(
            "Count", "Number of craft operations to perform", 1, 1, 64));

    private int crafted = 0;

    public QuickCraft() {
        super("QuickCraft", "Quick-crafts common recipes", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        crafted = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (crafted >= count.get()) { crafted = 0; return; }

        // Signal crafting via recipe book (simplified stub)
        // A full implementation requires mixin into the recipe book handler.
        // This module registers the intent; actual craft packets are sent via screen interaction.
        if (mc.currentScreen == null) {
            // Open inventory
            mc.player.sendMessage(
                    net.minecraft.text.Text.literal("[QuickCraft] Open crafting table to craft: " + recipe.get()),
                    true);
            crafted++;
        }
    }
}
