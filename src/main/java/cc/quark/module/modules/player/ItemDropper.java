package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ItemDropper extends Module {

    private final StringSetting blacklist = register(new StringSetting(
            "Blacklist", "Comma-separated list of item IDs to auto-drop (e.g. dirt,gravel,sand)",
            "dirt,gravel,sand,cobblestone,rotten_flesh"));

    private final BoolSetting autoSort = register(new BoolSetting(
            "AutoSort", "Sort remaining items after dropping", false));

    private int tickDelay = 0;

    public ItemDropper() {
        super("ItemDropper", "Auto-drops unwanted items from inventory", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        tickDelay++;
        if (tickDelay < 5) return;
        tickDelay = 0;

        Set<Item> blacklistedItems = parseBlacklist();
        var inv = mc.player.getInventory();

        for (int i = 9; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            if (!blacklistedItems.contains(stack.getItem())) continue;

            // Drop the entire stack
            mc.interactionManager.clickSlot(
                    mc.player.playerScreenHandler.syncId,
                    i, 1, SlotActionType.THROW, mc.player);
            return; // One drop per cycle
        }

        // Also check hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            if (!blacklistedItems.contains(stack.getItem())) continue;

            mc.interactionManager.clickSlot(
                    mc.player.playerScreenHandler.syncId,
                    36 + i, 1, SlotActionType.THROW, mc.player);
            return;
        }

        if (autoSort.isEnabled()) {
            sortInventory();
        }
    }

    private Set<Item> parseBlacklist() {
        Set<Item> items = new HashSet<>();
        String raw = blacklist.get();
        if (raw == null || raw.isBlank()) return items;

        for (String entry : raw.split(",")) {
            String trimmed = entry.trim().toLowerCase();
            if (trimmed.isEmpty()) continue;

            // Support both "dirt" and "minecraft:dirt"
            String id = trimmed.contains(":") ? trimmed : "minecraft:" + trimmed;
            Item item = Registries.ITEM.get(Identifier.of(id));
            if (item != net.minecraft.item.Items.AIR) {
                items.add(item);
            }
        }
        return items;
    }

    private void sortInventory() {
        var inv = mc.player.getInventory();
        // Single bubble-sort pass each cycle
        for (int i = 9; i < 35; i++) {
            ItemStack a = inv.getStack(i);
            ItemStack b = inv.getStack(i + 1);
            if (a.isEmpty() || b.isEmpty()) continue;

            // Push empties to the end
            if (a.isEmpty() && !b.isEmpty()) {
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        i, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        i + 1, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        i, 0, SlotActionType.PICKUP, mc.player);
                break;
            }
        }
    }
}
