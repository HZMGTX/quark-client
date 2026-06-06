package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.MinecraftClient;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * CoordsExport - Exports the player's current coordinates to the clipboard
 * and/or a file on demand (once per enable cycle).
 */
public class CoordsExport extends Module {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BoolSetting toClipboard = register(new BoolSetting(
            "To Clipboard", "Copy coordinates to the system clipboard", true));

    private final BoolSetting toFile = register(new BoolSetting(
            "To File", "Append coordinates to quark/coords.txt", true));

    private final StringSetting label = register(new StringSetting(
            "Label", "Optional label prepended to the coordinate entry", "Waypoint"));

    private final BoolSetting includeWorld = register(new BoolSetting(
            "Include World", "Append current dimension name to the export", true));

    private final BoolSetting autoDisable = register(new BoolSetting(
            "Auto Disable", "Disable this module after exporting once", true));

    private boolean exported = false;

    public CoordsExport() {
        super("CoordsExport", "Exports current coordinates to clipboard and/or file", Category.MISC);
    }

    @Override
    public void onEnable() {
        exported = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (exported) return;
        if (mc.player == null || mc.world == null) return;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        String dim = mc.world.getRegistryKey().getValue().getPath();

        String lbl = label.get().trim();
        String entry = (lbl.isEmpty() ? "" : "[" + lbl + "] ")
                + String.format("X: %.1f  Y: %.1f  Z: %.1f", x, y, z)
                + (includeWorld.isEnabled() ? "  [" + dim + "]" : "")
                + "  @ " + LocalDateTime.now().format(FMT);

        if (toClipboard.isEnabled()) {
            try {
                StringSelection sel = new StringSelection(entry);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
                ChatUtil.success("[CoordsExport] Copied to clipboard!");
            } catch (Exception e) {
                ChatUtil.error("[CoordsExport] Clipboard failed: " + e.getMessage());
            }
        }

        if (toFile.isEnabled()) {
            try {
                Path dir = MinecraftClient.getInstance().runDirectory.toPath().resolve("quark");
                Files.createDirectories(dir);
                try (FileWriter fw = new FileWriter(dir.resolve("coords.txt").toFile(), true)) {
                    fw.write(entry + System.lineSeparator());
                }
                ChatUtil.success("[CoordsExport] Saved to quark/coords.txt");
            } catch (IOException e) {
                ChatUtil.error("[CoordsExport] File write failed: " + e.getMessage());
            }
        }

        exported = true;
        if (autoDisable.isEnabled()) disable();
    }
}
