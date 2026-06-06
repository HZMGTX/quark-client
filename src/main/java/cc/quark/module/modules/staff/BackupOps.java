package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.MinecraftClient;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * BackupOps - Backs up the server ops list by running /ops and capturing the
 * output, then saving it to a local file and optionally printing it to chat.
 */
public class BackupOps extends Module {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BoolSetting saveToFile = register(new BoolSetting(
            "Save To File", "Save the ops list to quark/ops_backup.txt", true));

    private final BoolSetting printToChat = register(new BoolSetting(
            "Print To Chat", "Print the captured ops list locally", true));

    private final StringSetting opsCommand = register(new StringSetting(
            "Ops Command", "Command used to list operators", "ops"));

    private final BoolSetting captureResponse = register(new BoolSetting(
            "Capture Response", "Capture server response lines after running command", true));

    private boolean waitingForResponse = false;
    private int timeoutTicks = 0;
    private final List<String> captured = new ArrayList<>();

    public BackupOps() {
        super("BackupOps", "Backs up the server ops list by issuing the ops command and capturing output", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        captured.clear();
        waitingForResponse = captureResponse.isEnabled();
        timeoutTicks = 100; // 5 seconds to collect responses

        mc.player.networkHandler.sendChatCommand(opsCommand.get().trim());
        ChatUtil.info("[BackupOps] Issued /" + opsCommand.get() + " — collecting response...");
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!waitingForResponse) return;

        timeoutTicks--;
        if (timeoutTicks <= 0) {
            waitingForResponse = false;
            finalize_backup();
            disable();
        }
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!waitingForResponse || !event.isIncoming()) return;

        String msg = event.getMessage();
        if (msg != null && !msg.isEmpty()) {
            captured.add(msg);
        }
    }

    private void finalize_backup() {
        if (captured.isEmpty()) {
            ChatUtil.warn("[BackupOps] No response captured from server.");
            return;
        }

        String timestamp = LocalDateTime.now().format(FMT);

        if (printToChat.isEnabled()) {
            ChatUtil.info("[BackupOps] Captured " + captured.size() + " line(s):");
            for (String line : captured) {
                ChatUtil.info("  " + line);
            }
        }

        if (saveToFile.isEnabled()) {
            try {
                Path dir = MinecraftClient.getInstance().runDirectory.toPath().resolve("quark");
                Files.createDirectories(dir);
                try (FileWriter fw = new FileWriter(dir.resolve("ops_backup.txt").toFile(), true)) {
                    fw.write("=== Ops Backup @ " + timestamp + " ===" + System.lineSeparator());
                    for (String line : captured) {
                        fw.write(stripColor(line) + System.lineSeparator());
                    }
                    fw.write(System.lineSeparator());
                }
                ChatUtil.success("[BackupOps] Saved to quark/ops_backup.txt");
            } catch (IOException e) {
                ChatUtil.error("[BackupOps] File save failed: " + e.getMessage());
            }
        }
    }

    private String stripColor(String s) {
        return s.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }
}
