package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DimensionLogger extends Module {

    private final BoolSetting logFile  = register(new BoolSetting("Log to File", "Write dimension changes to a log file", true));
    private final BoolSetting chatMsg  = register(new BoolSetting("Chat Notify", "Show notification in chat on dimension change", true));
    private final BoolSetting showCoords = register(new BoolSetting("Show Coords", "Include coordinates in log entries", true));

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String lastDimension = "";

    public DimensionLogger() {
        super("DimensionLogger", "Logs dimension changes with coordinates", Category.MISC);
    }

    @Override
    public void onEnable() {
        lastDimension = "";
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        String current = mc.world.getRegistryKey().getValue().toString();

        if (!current.equals(lastDimension)) {
            String prev = lastDimension.isEmpty() ? "none" : lastDimension;
            lastDimension = current;

            String coords = showCoords.isEnabled()
                    ? String.format(" @ %.1f, %.1f, %.1f",
                            mc.player.getX(), mc.player.getY(), mc.player.getZ())
                    : "";

            String friendly = friendlyName(current);
            String entry = String.format("[%s] %s -> %s%s",
                    LocalDateTime.now().format(FMT),
                    friendlyName(prev), friendly, coords);

            if (chatMsg.isEnabled() && mc.inGameHud != null) {
                mc.inGameHud.getChatHud().addMessage(
                        net.minecraft.text.Text.literal(
                                "§e[DimLog] §7" + friendlyName(prev) +
                                " §f-> §b" + friendly + "§7" + coords));
            }

            if (logFile.isEnabled()) {
                try {
                    Path file = mc.runDirectory.toPath().resolve("quark").resolve("dimension_log.txt");
                    Files.createDirectories(file.getParent());
                    try (FileWriter fw = new FileWriter(file.toFile(), true)) {
                        fw.write(entry + System.lineSeparator());
                    }
                } catch (IOException ignored) {}
            }
        }
    }

    private String friendlyName(String dim) {
        return switch (dim) {
            case "minecraft:overworld"    -> "Overworld";
            case "minecraft:the_nether"   -> "Nether";
            case "minecraft:the_end"      -> "End";
            default -> dim.contains(":") ? dim.substring(dim.indexOf(':') + 1) : dim;
        };
    }
}
