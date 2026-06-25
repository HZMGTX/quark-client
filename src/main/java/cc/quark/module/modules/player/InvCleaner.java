package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * InvCleaner — automatically drops junk items from inventory.
 * Uses a configurable blacklist of item IDs to drop.
 */
public class InvCleaner extends Module {

    private final StringSetting junkList = register(new StringSetting(
            "Junk List",
            "Comma-separated item IDs to drop (e.g. dirt,gravel,flint)",
            "dirt,gravel,flint,rotten_flesh,spider_eye,bone,arrow,string"));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between drops", 500, 100, 5000));
    private final BoolSetting keepHotbar = register(new BoolSetting(
            "Keep Hotbar", "Never drop items currently in the hotbar slots 0-8", false));
    private final BoolSetting dropDamaged = register(new BoolSetting(
            "Drop Damaged Tools", "Drop tools with durability below 10%", false));

    private final TimerUtil timer = new TimerUtil();

    public InvCleaner() {
        super("InvCleaner", "Drops junk items from inventory automatically", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    private Set<String> getJunkSet() {
        Set<String> set = new HashSet<>();
        for (String s : junkList.get().split(",")) {
            String trimmed = s.trim().toLowerCase();
            if (!trimmed.isEmpty()) set.add(trimmed);
        }
        return set;
    }

    private boolean isJunk(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String id = Registries.ITEM.getId(stack.getItem()).getPath();
        Set<String> junk = getJunkSet();
        if (junk.contains(id)) return true;

        if (dropDamaged.isEnabled()) {
            if (stack.isDamageable()) {
                int max = stack.getMaxDamage();
                int dmg = stack.getDamage();
                if (max > 0 && (double)(max - dmg) / max < 0.10) return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return; // don't clean while GUI is open
        if (!timer.hasReached(delay.get())) return;

        var handler = mc.player.playerScreenHandler;
        int startSlot = keepHotbar.isEnabled() ? 9 : 0;

        for (int i = startSlot; i < handler.slots.size(); i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (isJunk(stack)) {
                mc.interactionManager.clickSlot(handler.syncId, i, 1, SlotActionType.THROW, mc.player);
                timer.reset();
                return;
            }
        }
    }
}
