package cc.quark.module.modules.misc;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;

public class ModuleHistory extends Module {

    private final IntSetting maxEntries = register(new IntSetting(
            "Max Entries", "How many toggle events to show", 5, 1, 10));
    private final IntSetting posX = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting posY = register(new IntSetting("Y", "HUD Y position", 150, 0, 3000));

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private record HistoryEntry(String moduleName, boolean enabled, String time) {}

    private final Deque<HistoryEntry> history = new ArrayDeque<>();

    // Track previous states
    private final java.util.Map<String, Boolean> prevStates = new java.util.HashMap<>();

    public ModuleHistory() {
        super("ModuleHistory", "Logs recently toggled modules with timestamps and shows them in the HUD", Category.MISC);
    }

    @Override
    public void onEnable() {
        history.clear();
        prevStates.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        Quark q = Quark.getInstance();
        if (q == null) return;

        for (Module mod : q.getModuleManager().getModules()) {
            if (mod == this) continue;
            String name = mod.getName();
            boolean enabled = mod.isEnabled();
            Boolean prev = prevStates.get(name);
            if (prev == null) {
                prevStates.put(name, enabled);
                continue;
            }
            if (prev != enabled) {
                prevStates.put(name, enabled);
                String time = LocalTime.now().format(TIME_FMT);
                history.addFirst(new HistoryEntry(name, enabled, time));
                while (history.size() > maxEntries.get()) history.removeLast();
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (history.isEmpty()) return;
        DrawContext ctx = event.getDrawContext();
        int x = posX.get();
        int y = posY.get();
        int lh = mc.textRenderer.fontHeight + 2;

        ctx.drawTextWithShadow(mc.textRenderer, "§7Module History:", x, y, 0xFFFFFFFF);
        y += lh;

        int i = 0;
        for (HistoryEntry entry : history) {
            if (i >= maxEntries.get()) break;
            String color = entry.enabled() ? "§a" : "§c";
            String text = "§7[" + entry.time() + "] " + color + entry.moduleName()
                    + " §7" + (entry.enabled() ? "ON" : "OFF");
            ctx.drawTextWithShadow(mc.textRenderer, text, x, y, 0xFFFFFFFF);
            y += lh;
            i++;
        }
    }
}
