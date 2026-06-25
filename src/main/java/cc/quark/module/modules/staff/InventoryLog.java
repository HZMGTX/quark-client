package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class InventoryLog extends Module {

    private final BoolSetting logToChat = register(new BoolSetting(
            "Log To Chat", "Print inventory changes in local chat", true));
    private final BoolSetting logToFile = register(new BoolSetting(
            "Log To File", "Write inventory changes to a log file", true));
    private final IntSetting minCount = register(new IntSetting(
            "Min Count", "Minimum item count change to log", 1, 1, 64));

    private final Map<Integer, ItemStack> lastKnown = new HashMap<>();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public InventoryLog() {
        super("InventoryLog", "Logs player inventory slot changes to chat and file", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        lastKnown.clear();
        // Snapshot current inventory
        if (mc.player != null) {
            for (int i = 0; i < mc.player.getInventory().size(); i++) {
                lastKnown.put(i, mc.player.getInventory().getStack(i).copy());
            }
        }
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof ScreenHandlerSlotUpdateS2CPacket pkt)) return;
        if (mc.player == null) return;

        int slot = pkt.getSlot();
        ItemStack newStack = pkt.getStack();
        if (newStack == null) return;

        ItemStack old = lastKnown.get(slot);
        boolean changed = false;

        if (old == null || old.isEmpty() != newStack.isEmpty()) {
            changed = true;
        } else if (!old.isEmpty() && !ItemStack.areItemsEqual(old, newStack)) {
            changed = true;
        } else if (!old.isEmpty() && Math.abs(old.getCount() - newStack.getCount()) >= minCount.get()) {
            changed = true;
        }

        if (changed) {
            String oldStr = old == null || old.isEmpty() ? "empty"
                    : old.getCount() + "x " + old.getName().getString();
            String newStr = newStack.isEmpty() ? "empty"
                    : newStack.getCount() + "x " + newStack.getName().getString();
            String entry = "Slot " + slot + ": [" + oldStr + "] -> [" + newStr + "]";

            if (logToChat.isEnabled()) ChatUtil.info("[InvLog] " + entry);
            if (logToFile.isEnabled()) writeToLog(entry);

            lastKnown.put(slot, newStack.copy());
        }
    }

    private void writeToLog(String entry) {
        try {
            File dir = new File(MinecraftClient.getInstance().runDirectory, "quark");
            if (!dir.exists()) dir.mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(new File(dir, "invlog.log"), true))) {
                pw.println("[" + DATE_FORMAT.format(new Date()) + "] " + entry);
            }
        } catch (IOException ignored) {}
    }
}
