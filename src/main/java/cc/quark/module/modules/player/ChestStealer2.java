package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ChestStealer2 - Enhanced chest stealer with item filtering, whitelist/blacklist
 * modes, configurable delay, and auto-close when done.
 */
public class ChestStealer2 extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between each steal action", 2, 1, 20));

    private final BoolSetting autoClose = register(new BoolSetting(
            "Auto Close", "Close the chest when all desired items have been taken", true));

    private final BoolSetting filterEnabled = register(new BoolSetting(
            "Filter", "Enable item name filter (whitelist or blacklist)", false));

    private final BoolSetting whitelistMode = register(new BoolSetting(
            "Whitelist Mode", "When filter enabled: true=whitelist, false=blacklist", true));

    private final StringSetting filterList = register(new StringSetting(
            "Filter List", "Comma-separated item IDs or partial names (e.g. diamond,sword,arrow)", "diamond,emerald"));

    private final BoolSetting skipDurability = register(new BoolSetting(
            "Skip Damaged", "Skip items with damaged durability below threshold", false));

    private final IntSetting durabilityThreshold = register(new IntSetting(
            "Min Durability", "Minimum durability % to accept (when Skip Damaged enabled)", 20, 1, 100));

    private final BoolSetting notifyCount = register(new BoolSetting(
            "Notify Count", "Print how many items were stolen when finished", true));

    private int ticksSinceLast = 0;
    private int stolenCount = 0;

    public ChestStealer2() {
        super("ChestStealer2", "Enhanced chest stealer with item filtering and auto-close", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        stolenCount = 0;
        ticksSinceLast = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (!(mc.currentScreen instanceof GenericContainerScreen)
                && !(mc.currentScreen instanceof ShulkerBoxScreen)) {
            return;
        }

        ticksSinceLast++;
        if (ticksSinceLast < delay.get()) return;
        ticksSinceLast = 0;

        GenericContainerScreenHandler handler =
                (GenericContainerScreenHandler) mc.player.currentScreenHandler;
        List<Slot> slots = handler.slots;
        int containerSize = handler.getRows() * 9;

        // Find the first matching slot and steal it
        for (int i = 0; i < containerSize; i++) {
            Slot slot = slots.get(i);
            if (!slot.hasStack()) continue;

            ItemStack stack = slot.getStack();
            if (!shouldTake(stack)) continue;

            mc.interactionManager.clickSlot(
                    handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            stolenCount++;
            return;
        }

        // Nothing left to steal
        if (notifyCount.isEnabled() && stolenCount > 0) {
            ChatUtil.info("[ChestStealer2] Stolen " + stolenCount + " item stacks.");
            stolenCount = 0;
        }
        if (autoClose.isEnabled()) {
            mc.player.closeHandledScreen();
        }
    }

    private boolean shouldTake(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // Durability check
        if (skipDurability.isEnabled() && stack.isDamageable()) {
            int maxDmg = stack.getMaxDamage();
            if (maxDmg > 0) {
                int durabilityPct = (int) (((float)(maxDmg - stack.getDamage()) / maxDmg) * 100);
                if (durabilityPct < durabilityThreshold.get()) return false;
            }
        }

        if (!filterEnabled.isEnabled()) return true;

        String itemName = stack.getItem().toString().toLowerCase();
        String displayName = stack.getName().getString().toLowerCase();
        Set<String> filters = parseFilterList();

        boolean matches = filters.stream().anyMatch(f ->
                itemName.contains(f) || displayName.contains(f));

        return whitelistMode.isEnabled() == matches;
    }

    private Set<String> parseFilterList() {
        Set<String> result = new HashSet<>();
        String raw = filterList.get().trim();
        if (raw.isEmpty()) return result;
        Arrays.stream(raw.split(","))
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .forEach(result::add);
        return result;
    }
}
