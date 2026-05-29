package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.InventoryUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Items;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoCraft extends Module {

    private final ModeSetting recipe = register(new ModeSetting(
            "Recipe", "Item to auto-craft",
            "Planks", "Planks", "Sticks", "Crafting Table", "Chest"));
    private final BoolSetting notify = register(new BoolSetting(
            "Notify", "Chat message when materials are available", true));
    private final BoolSetting autoCraft = register(new BoolSetting(
            "Auto Craft", "Actually craft when a crafting table is open", false));

    private boolean announced = false;
    private final TimerUtil craftTimer = new TimerUtil();

    public AutoCraft() {
        super("AutoCraft", "Detects when crafting materials are available; optionally auto-crafts", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        announced = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean canCraft = hasEnoughMaterials();

        if (canCraft) {
            if (notify.isEnabled() && !announced) {
                ChatUtil.info("Enough materials to craft " + recipe.get());
                announced = true;
            }
            // Auto-craft if a crafting screen is open
            if (autoCraft.isEnabled() && craftTimer.hasReached(500)
                    && mc.player.currentScreenHandler instanceof CraftingScreenHandler) {
                tryCraft();
                craftTimer.reset();
            }
        } else {
            announced = false;
        }
    }

    private boolean hasEnoughMaterials() {
        return switch (recipe.get()) {
            case "Planks"         -> InventoryUtil.countItem(Items.OAK_LOG) >= 1
                                  || InventoryUtil.countItem(Items.SPRUCE_LOG) >= 1;
            case "Sticks"         -> InventoryUtil.countItem(Items.OAK_PLANKS) >= 2
                                  || InventoryUtil.countItem(Items.SPRUCE_PLANKS) >= 2;
            case "Crafting Table" -> InventoryUtil.countItem(Items.OAK_PLANKS) >= 4;
            case "Chest"          -> InventoryUtil.countItem(Items.OAK_PLANKS) >= 8;
            default               -> false;
        };
    }

    /** Shift-click the crafting output slot (slot 0 in CraftingScreenHandler). */
    private void tryCraft() {
        if (mc.interactionManager == null) return;
        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                0, 0, SlotActionType.QUICK_MOVE, mc.player);
    }
}
