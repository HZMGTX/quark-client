package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

public class ChestStealer extends Module {

    private final IntSetting delay = register(new IntSetting("Delay", "Milliseconds between steals", 100, 50, 500));
    private final BoolSetting closeAfter = register(new BoolSetting("Close After", "Close container when done stealing", false));
    private final ModeSetting filter = register(new ModeSetting("Filter", "Item filter mode", "All", "All", "Valuable", "Weapons"));
    private final BoolSetting ignoreEmpty = register(new BoolSetting("Skip Empty", "Skip slots with no matching items instead of stalling", true));

    private final TimerUtil timer = new TimerUtil();
    private boolean doneThisOpen = false;

    public ChestStealer() {
        super("ChestStealer", "Automatically moves items from open containers into player inventory", Category.WORLD);
    }

    @Override
    public void onEnable() {
        doneThisOpen = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (!(mc.currentScreen instanceof HandledScreen<?>)) {
            doneThisOpen = false;
            return;
        }

        if (!timer.hasReached(delay.get())) return;

        var handler = mc.player.currentScreenHandler;
        int containerSize = handler.slots.size() - 36;
        if (containerSize <= 0) return;

        List<Slot> slots = handler.slots;

        for (int i = 0; i < containerSize; i++) {
            Slot slot = slots.get(i);
            if (!slot.hasStack()) continue;
            if (!passesFilter(slot.getStack().getItem())) continue;
            if (!slot.canTakeItems(mc.player)) continue;

            mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            timer.reset();
            return;
        }

        if (closeAfter.isEnabled() && !doneThisOpen) {
            doneThisOpen = true;
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(handler.syncId));
            mc.currentScreen.close();
        }
    }

    private boolean passesFilter(Item item) {
        return switch (filter.get()) {
            case "Valuable" -> isValuable(item);
            case "Weapons" -> isWeapon(item);
            default -> true;
        };
    }

    private boolean isValuable(Item item) {
        return item == Items.DIAMOND || item == Items.EMERALD || item == Items.GOLD_INGOT
                || item == Items.IRON_INGOT || item == Items.NETHERITE_INGOT
                || item == Items.NETHERITE_SCRAP || item == Items.ANCIENT_DEBRIS
                || item == Items.DIAMOND_BLOCK || item == Items.EMERALD_BLOCK
                || item == Items.GOLD_BLOCK || item == Items.IRON_BLOCK
                || item == Items.DIAMOND_SWORD || item == Items.DIAMOND_PICKAXE
                || item == Items.DIAMOND_AXE || item == Items.DIAMOND_SHOVEL
                || item == Items.NETHERITE_SWORD || item == Items.NETHERITE_PICKAXE
                || item == Items.NETHERITE_AXE || item == Items.NETHERITE_SHOVEL
                || (item instanceof ArmorItem ai && isGoodTier(ai.getMaterial().toString()));
    }

    private boolean isWeapon(Item item) {
        return item instanceof SwordItem || item instanceof AxeItem
                || item instanceof BowItem || item instanceof CrossbowItem
                || item instanceof TridentItem;
    }

    private boolean isGoodTier(String material) {
        return material.contains("diamond") || material.contains("netherite");
    }
}
