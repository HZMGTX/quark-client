package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AutoDropBlacklist extends Module {

    private final StringSetting blacklist = register(new StringSetting(
            "Blacklist", "Comma-separated item names to auto-drop", "cobblestone,dirt"));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between drop actions", 500, 100, 5000));

    private final TimerUtil timer = new TimerUtil();

    public AutoDropBlacklist() {
        super("AutoDropBlacklist", "Automatically drops blacklisted items from inventory", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;
        timer.reset();

        Set<String> names = getBlacklistSet();
        if (names.isEmpty()) return;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            String itemName = stack.getName().getString().toLowerCase();
            boolean matches = names.stream().anyMatch(itemName::contains);
            if (!matches) continue;

            int guiSlot = i < 9 ? 36 + i : i;
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    guiSlot, 1, SlotActionType.THROW, mc.player);
            return;
        }
    }

    private Set<String> getBlacklistSet() {
        Set<String> set = new HashSet<>();
        String raw = blacklist.get().trim();
        if (!raw.isEmpty()) {
            for (String entry : raw.split(",")) {
                String trimmed = entry.trim().toLowerCase();
                if (!trimmed.isEmpty()) set.add(trimmed);
            }
        }
        return set;
    }
}
