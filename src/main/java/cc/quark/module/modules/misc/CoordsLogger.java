package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import org.lwjgl.glfw.GLFW;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CoordsLogger extends Module {

    private final BoolSetting autoSave = register(new BoolSetting(
            "AutoSave", "Automatically save coords every 60 seconds", false));

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private long lastAutoSave = 0;

    public CoordsLogger() {
        super("CoordsLogger", "Saves current coordinates to a log file on key press", Category.MISC);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (event.getKeyCode() == GLFW.GLFW_KEY_INSERT) {
            saveCoords();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!autoSave.isEnabled() || mc.player == null) return;
        long now = System.currentTimeMillis();
        if (now - lastAutoSave >= 60_000L) {
            saveCoords();
            lastAutoSave = now;
        }
    }

    private void saveCoords() {
        if (mc.player == null) return;
        String timestamp = LocalDateTime.now().format(FMT);
        double x = mc.player.getX(), y = mc.player.getY(), z = mc.player.getZ();
        String dim = mc.world != null ? mc.world.getRegistryKey().getValue().getPath() : "unknown";
        String line = String.format("[%s] %s: X=%.1f Y=%.1f Z=%.1f", timestamp, dim, x, y, z);
        try (PrintWriter pw = new PrintWriter(new FileWriter("quark_coords.log", true))) {
            pw.println(line);
        } catch (IOException ignored) {}
        if (mc.player != null) {
            mc.player.sendMessage(net.minecraft.text.Text.literal("§7[CoordsLogger] §aSaved: §f" + line), false);
        }
    }
}
