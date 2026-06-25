package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.DrawContext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class CombatLog extends Module {

    private final IntSetting  posX       = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting  posY       = register(new IntSetting("Y", "HUD Y position", 80, 0, 3000));
    private final IntSetting  maxEntries = register(new IntSetting("Max Entries", "Maximum lines shown in log", 8, 1, 20));
    private final IntSetting  fadeTime   = register(new IntSetting("Fade Time", "Seconds before entries fade (0 = never)", 30, 0, 120));
    private final BoolSetting showTime   = register(new BoolSetting("Timestamps", "Show timestamp for each entry", true));
    private final BoolSetting bgBox      = register(new BoolSetting("Background", "Draw dark background box", true));

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static class LogEntry {
        final String text;
        final int color;
        final TimerUtil timer = new TimerUtil();
        LogEntry(String text, int color) { this.text = text; this.color = color; timer.reset(); }
    }

    private final Deque<LogEntry> log = new ArrayDeque<>();

    public CombatLog() {
        super("CombatLog", "Logs combat events (hits, damage, kills) on screen", Category.RENDER);
    }

    @Override
    public void onEnable() {
        log.clear();
    }

    @Override
    public void onDisable() {
        log.clear();
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null) return;
        float amount = event.getAmount();
        String timeStr = showTime.isEnabled() ? "[" + LocalTime.now().format(TIME_FMT) + "] " : "";
        // EventDamage fires when the local player takes damage
        String sourceName = event.getSource().getType().msgId();
        addEntry(timeStr + String.format("Took %.1f dmg [%s]", amount, sourceName), 0xFFFF5555);
    }

    private void addEntry(String text, int color) {
        log.addFirst(new LogEntry(text, color));
        while (log.size() > maxEntries.get()) log.removeLast();
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        int fade = fadeTime.get();
        if (fade > 0) log.removeIf(e -> e.timer.hasReached(fade * 1000L));
        if (log.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        List<LogEntry> visible = new ArrayList<>(log);
        int count = Math.min(visible.size(), maxEntries.get());
        int lh = mc.textRenderer.fontHeight + 2;
        int x = posX.get(), y = posY.get();

        if (bgBox.isEnabled()) {
            int maxW = 0;
            for (int i = 0; i < count; i++) maxW = Math.max(maxW, mc.textRenderer.getWidth(visible.get(i).text));
            ctx.fill(x - 2, y - 2, x + maxW + 2, y + count * lh + 1, 0x88000000);
        }

        for (int i = 0; i < count; i++) {
            LogEntry e = visible.get(i);
            ctx.drawTextWithShadow(mc.textRenderer, e.text, x, y, e.color);
            y += lh;
        }
    }
}
