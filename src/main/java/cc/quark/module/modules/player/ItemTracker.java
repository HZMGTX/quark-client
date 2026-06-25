package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ItemTracker extends Module {

    private final BoolSetting logChanges = register(new BoolSetting(
            "LogChanges", "Log inventory changes to chat", true));

    private final Map<Integer, String> previousInventory = new HashMap<>();
    private int tickCounter = 0;

    public ItemTracker() {
        super("ItemTracker", "Tracks inventory changes and logs added/removed items", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        previousInventory.clear();
        tickCounter = 0;
        if (mc.player != null) {
            snapshotInventory();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;

        Map<Integer, String> current = captureInventory();

        if (logChanges.isEnabled()) {
            for (Map.Entry<Integer, String> entry : current.entrySet()) {
                String prev = previousInventory.get(entry.getKey());
                if (!entry.getValue().equals(prev)) {
                    if (prev == null) {
                        ChatUtil.info("[ItemTracker] Added: " + entry.getValue() + " (slot " + entry.getKey() + ")");
                    } else {
                        ChatUtil.info("[ItemTracker] Changed slot " + entry.getKey() + ": " + prev + " -> " + entry.getValue());
                    }
                }
            }
            for (Map.Entry<Integer, String> entry : previousInventory.entrySet()) {
                if (!current.containsKey(entry.getKey())) {
                    ChatUtil.info("[ItemTracker] Removed: " + entry.getValue() + " (slot " + entry.getKey() + ")");
                }
            }
        }

        previousInventory.clear();
        previousInventory.putAll(current);
    }

    private Map<Integer, String> captureInventory() {
        Map<Integer, String> snapshot = new HashMap<>();
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                snapshot.put(i, stack.getCount() + "x " + stack.getItem().toString());
            }
        }
        return snapshot;
    }

    private void snapshotInventory() {
        previousInventory.clear();
        previousInventory.putAll(captureInventory());
    }
}
