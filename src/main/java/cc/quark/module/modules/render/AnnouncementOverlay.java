package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class AnnouncementOverlay extends Module {

    private final IntSetting posX      = register(new IntSetting("X", "HUD X position", 10, 0, 3000));
    private final IntSetting posY      = register(new IntSetting("Y", "HUD Y position", 60, 0, 3000));
    private final IntSetting duration  = register(new IntSetting("Duration", "How long each message is shown (ms)", 5000, 1000, 30000));
    private final IntSetting maxLines  = register(new IntSetting("Max Lines", "Maximum concurrent announcements shown", 4, 1, 10));
    private final BoolSetting bgBox    = register(new BoolSetting("Background", "Draw background box behind text", true));

    /** Keywords that mark a message as a server announcement. */
    private static final String[] TRIGGERS = {
        "[announce]", "[broadcast]", "[alert]", "[info]", "[notice]",
        "***", "===", "---", "[server]", "[admin]"
    };

    private static class Entry {
        final String text;
        final TimerUtil timer = new TimerUtil();
        Entry(String text) { this.text = text; timer.reset(); }
    }

    private final Deque<Entry> entries = new ArrayDeque<>();

    public AnnouncementOverlay() {
        super("AnnouncementOverlay", "Shows server announcements as an on-screen overlay", Category.RENDER);
    }

    @Override
    public void onEnable() {
        entries.clear();
    }

    @Override
    public void onDisable() {
        entries.clear();
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage();
        String lower = msg.toLowerCase();
        for (String trigger : TRIGGERS) {
            if (lower.contains(trigger.toLowerCase())) {
                entries.addFirst(new Entry(msg));
                while (entries.size() > maxLines.get()) entries.removeLast();
                return;
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        // Expire old entries
        entries.removeIf(e -> e.timer.hasReached(duration.get()));
        if (entries.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        int x = posX.get();
        int y = posY.get();
        int lh = mc.textRenderer.fontHeight + 2;

        List<Entry> visible = new ArrayList<>(entries);
        int count = Math.min(visible.size(), maxLines.get());

        for (int i = 0; i < count; i++) {
            String text = visible.get(i).text;
            if (bgBox.isEnabled()) {
                int w = mc.textRenderer.getWidth(text) + 4;
                ctx.fill(x - 2, y - 1, x + w, y + mc.textRenderer.fontHeight + 1, 0x88000000);
            }
            ctx.drawTextWithShadow(mc.textRenderer, text, x, y, 0xFFFFFF55);
            y += lh;
        }
    }
}
