package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * KeyLogger - EDUCATIONAL USE ONLY. Logs key presses to chat or file
 * for macro recording and debugging keybind conflicts.
 */
public class KeyLogger extends Module {

    private final BoolSetting toChat = register(new BoolSetting(
            "To Chat", "Print key codes to local chat (client-only)", false));

    private final BoolSetting toFile = register(new BoolSetting(
            "To File", "Save key log to .minecraft/quark/keylog.txt", true));

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private FileWriter writer;

    public KeyLogger() {
        super("KeyLogger", "Logs keypress events for macros (educational)", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (toFile.isEnabled()) {
            try {
                Path logFile = mc.runDirectory.toPath().resolve("quark").resolve("keylog.txt");
                Files.createDirectories(logFile.getParent());
                writer = new FileWriter(logFile.toFile(), true);
            } catch (IOException e) {
                writer = null;
            }
        }
    }

    @Override
    public void onDisable() {
        closeWriter();
    }

    @EventHandler
    public void onKey(EventKey event) {
        int keyCode = event.getKeyCode();
        String entry = "[" + LocalDateTime.now().format(FMT) + "] KEY:" + keyCode;

        if (toChat.isEnabled()) {
            ChatUtil.addMessage("KeyLogger: KEY " + keyCode);
        }

        if (toFile.isEnabled() && writer != null) {
            try {
                writer.write(entry + System.lineSeparator());
                writer.flush();
            } catch (IOException ignored) {}
        }
    }

    private void closeWriter() {
        if (writer != null) {
            try { writer.close(); } catch (IOException ignored) {}
            writer = null;
        }
    }
}
