package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * InventorySnapshot - Takes a snapshot of the player's inventory and
 * compares it against the current state each tick, reporting differences.
 * Enable once to capture the snapshot; enable again to compare and report.
 */
public class InventorySnapshot extends Module {

    private final BoolSetting autoPrint = register(new BoolSetting(
            "Auto Print", "Automatically print diff when items change", false));

    private final BoolSetting printAdded = register(new BoolSetting(
            "Print Added", "Show items that were added since snapshot", true));

    private final BoolSetting printRemoved = register(new BoolSetting(
            "Print Removed", "Show items that were removed since snapshot", true));

    private final BoolSetting silent = register(new BoolSetting(
            "Silent Snapshot", "Take snapshot silently without any chat message", false));

    // Slot-indexed snapshots: index 0-35 = inventory, 36-39 = armor, 40 = offhand
    private List<SnapshotEntry> snapshot = null;
    private boolean snapshotTaken = false;
    private boolean comparisonDone = false;

    public InventorySnapshot() {
        super("InventorySnapshot", "Snapshots inventory for later comparison to detect changes", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (!snapshotTaken) {
            takeSnapshot();
        } else {
            compareAndReport();
            comparisonDone = true;
        }
    }

    @Override
    public void onDisable() {
        if (comparisonDone) {
            // Reset so next enable takes a fresh snapshot
            snapshot      = null;
            snapshotTaken = false;
            comparisonDone = false;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!autoPrint.isEnabled()) return;
        if (mc.player == null || snapshot == null) return;

        List<SnapshotEntry> current = captureInventory();
        boolean changed = !entriesMatch(snapshot, current);
        if (changed) {
            diffAndPrint(snapshot, current);
            snapshot = current; // update to current so we only print new diffs
        }
    }

    private void takeSnapshot() {
        if (mc.player == null) return;
        snapshot = captureInventory();
        snapshotTaken = true;
        if (!silent.isEnabled()) {
            ChatUtil.info("[InventorySnapshot] Snapshot taken (" + countNonEmpty(snapshot) + " stacks).");
        }
        disable();
    }

    private void compareAndReport() {
        if (mc.player == null || snapshot == null) return;
        List<SnapshotEntry> current = captureInventory();
        diffAndPrint(snapshot, current);
        disable();
    }

    private void diffAndPrint(List<SnapshotEntry> before, List<SnapshotEntry> after) {
        for (int i = 0; i < Math.min(before.size(), after.size()); i++) {
            SnapshotEntry b = before.get(i);
            SnapshotEntry a = after.get(i);

            if (b.itemId.equals(a.itemId)) {
                int diff = a.count - b.count;
                if (diff > 0 && printAdded.isEnabled()) {
                    ChatUtil.success("[InventorySnapshot] Slot " + i + ": +" + diff + "x " + a.itemId);
                } else if (diff < 0 && printRemoved.isEnabled()) {
                    ChatUtil.warn("[InventorySnapshot] Slot " + i + ": " + diff + "x " + b.itemId);
                }
            } else {
                // Item type changed in slot
                if (!b.itemId.isEmpty() && printRemoved.isEnabled()) {
                    ChatUtil.warn("[InventorySnapshot] Slot " + i + ": removed " + b.count + "x " + b.itemId);
                }
                if (!a.itemId.isEmpty() && printAdded.isEnabled()) {
                    ChatUtil.success("[InventorySnapshot] Slot " + i + ": added " + a.count + "x " + a.itemId);
                }
            }
        }
    }

    private List<SnapshotEntry> captureInventory() {
        List<SnapshotEntry> entries = new ArrayList<>();
        for (int i = 0; i < 41; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            String id = stack.isEmpty() ? "" : stack.getItem().toString();
            entries.add(new SnapshotEntry(id, stack.isEmpty() ? 0 : stack.getCount()));
        }
        return entries;
    }

    private boolean entriesMatch(List<SnapshotEntry> a, List<SnapshotEntry> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).equals(b.get(i))) return false;
        }
        return true;
    }

    private int countNonEmpty(List<SnapshotEntry> entries) {
        return (int) entries.stream().filter(e -> !e.itemId.isEmpty()).count();
    }

    private static class SnapshotEntry {
        final String itemId;
        final int    count;

        SnapshotEntry(String itemId, int count) {
            this.itemId = itemId;
            this.count  = count;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SnapshotEntry)) return false;
            SnapshotEntry s = (SnapshotEntry) o;
            return count == s.count && itemId.equals(s.itemId);
        }

        @Override
        public int hashCode() {
            return 31 * itemId.hashCode() + count;
        }
    }
}
