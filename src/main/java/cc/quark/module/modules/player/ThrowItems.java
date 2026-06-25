package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * ThrowItems - Automatically drops (throws) items matching a configurable list
 * from the player's inventory at a configurable rate.
 */
public class ThrowItems extends Module {

    private final StringSetting itemList = register(new StringSetting(
            "Items", "Comma-separated item IDs or partial names to throw (e.g. cobblestone,dirt)", "cobblestone,dirt"));

    private final BoolSetting throwStack = register(new BoolSetting(
            "Throw Stack", "Throw the whole stack at once (false = throw one at a time)", true));

    private final BoolSetting hotbarOnly = register(new BoolSetting(
            "Hotbar Only", "Only throw items from the hotbar (slots 0-8)", false));

    private final IntSetting delayTicks = register(new IntSetting(
            "Delay", "Ticks between each throw action", 5, 1, 40));

    private final BoolSetting notify = register(new BoolSetting(
            "Notify", "Print a message when an item is thrown", false));

    private int tickCooldown = 0;

    public ThrowItems() {
        super("ThrowItems", "Automatically throws matched items from the inventory", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (tickCooldown > 0) {
            tickCooldown--;
            return;
        }

        Set<String> filters = parseList();
        if (filters.isEmpty()) return;

        int startSlot = 0;
        int endSlot   = hotbarOnly.isEnabled() ? 9 : 36;

        for (int i = startSlot; i < endSlot; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            String id   = stack.getItem().toString().toLowerCase();
            String name = stack.getName().getString().toLowerCase();
            boolean matches = filters.stream().anyMatch(f -> id.contains(f) || name.contains(f));
            if (!matches) continue;

            // Convert inventory index to screen slot index
            // Hotbar slots 0-8 map to screen slots 36-44; inventory 9-35 map to 9-35
            int screenSlot;
            if (i < 9) {
                screenSlot = 36 + i; // hotbar
            } else {
                screenSlot = i;      // main inventory
            }

            // Drop key: button 0 = drop one, button 1 = drop stack
            int button = throwStack.isEnabled() ? 1 : 0;
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    screenSlot, button, SlotActionType.THROW, mc.player);

            if (notify.isEnabled()) {
                ChatUtil.info("[ThrowItems] Threw " + stack.getName().getString());
            }

            tickCooldown = delayTicks.get();
            return; // one action per cooldown
        }
    }

    private Set<String> parseList() {
        Set<String> result = new HashSet<>();
        String raw = itemList.get().trim();
        if (raw.isEmpty()) return result;
        Arrays.stream(raw.split(","))
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .forEach(result::add);
        return result;
    }
}
