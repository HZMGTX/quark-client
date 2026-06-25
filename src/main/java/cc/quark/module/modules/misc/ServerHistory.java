package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;

public class ServerHistory extends Module {

    private final IntSetting maxHistory = register(new IntSetting(
            "Max History", "Maximum number of servers to remember", 10, 1, 50));

    private final BoolSetting showOnHUD = register(new BoolSetting(
            "Show on HUD", "Display recent server list on screen", false));

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final Deque<String> history = new ArrayDeque<>();
    private String lastServer = "";

    public ServerHistory() {
        super("ServerHistory", "Logs recently visited servers", Category.MISC);
    }

    @Override
    public void onEnable() {
        lastServer = "";
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        String current = getCurrentServer();
        if (current.isEmpty() || current.equals(lastServer)) return;

        lastServer = current;
        String entry = "[" + LocalDateTime.now().format(FMT) + "] " + current;

        history.addFirst(entry);
        while (history.size() > maxHistory.get()) history.removeLast();

        // Persist to file
        try {
            Path file = mc.runDirectory.toPath().resolve("quark").resolve("server_history.txt");
            Files.createDirectories(file.getParent());
            try (FileWriter fw = new FileWriter(file.toFile(), true)) {
                fw.write(entry + System.lineSeparator());
            }
        } catch (IOException ignored) {}
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showOnHUD.isEnabled() || history.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int lh = mc.textRenderer.fontHeight + 1;
        int startY = 4;

        ctx.drawTextWithShadow(mc.textRenderer, "§7Recent Servers:", 4, startY, 0xFFFFFFFF);
        startY += lh + 2;

        int count = 0;
        for (String entry : history) {
            if (count++ >= Math.min(maxHistory.get(), 5)) break;
            ctx.drawTextWithShadow(mc.textRenderer, "§8" + entry, 4, startY, 0xFFAAAAAA);
            startY += lh;
        }
    }

    private String getCurrentServer() {
        if (mc.getCurrentServerEntry() != null) return mc.getCurrentServerEntry().address;
        return "";
    }
}
