package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationOverlay extends Module {

    public enum NotifType { SUCCESS, ERROR, INFO, WARNING }

    public static class Notification {
        public final String title, message;
        public final NotifType type;
        public int ticks;
        public Notification(String title, String message, NotifType type, int ticks) {
            this.title = title; this.message = message;
            this.type = type;   this.ticks = ticks;
        }
    }

    private static final List<Notification> QUEUE = new ArrayList<>();

    public static void send(String title, String message, NotifType type) {
        QUEUE.add(new Notification(title, message, type, 80));
    }

    private final IntSetting maxShown = register(new IntSetting(
            "Max", "Maximum notifications on screen at once", 5, 1, 10));
    private final BoolSetting timerBar = register(new BoolSetting(
            "Timer Bar", "Show a countdown bar under each notification", true));

    public NotificationOverlay() {
        super("NotificationOverlay", "In-game toast-style HUD notifications", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int screenW = mc.getWindow().getScaledWidth();
        int y = 10;
        int shown = 0;

        Iterator<Notification> it = QUEUE.iterator();
        while (it.hasNext() && shown < maxShown.get()) {
            Notification n = it.next();
            if (--n.ticks <= 0) { it.remove(); continue; }

            int accentColor = switch (n.type) {
                case SUCCESS -> 0xFF44FF44;
                case ERROR   -> 0xFFFF4444;
                case WARNING -> 0xFFFFAA00;
                case INFO    -> 0xFF4488FF;
            };

            int x = screenW - 162;
            ctx.fill(x - 2, y - 2, x + 160, y + 24, 0x99000000);
            ctx.fill(x - 2, y - 2, x,        y + 24, accentColor);
            ctx.drawText(mc.textRenderer, Text.literal(n.title),   x + 3, y,      accentColor, true);
            ctx.drawText(mc.textRenderer, Text.literal(n.message), x + 3, y + 10, 0xFFFFFFFF,  false);

            if (timerBar.isEnabled()) {
                float frac = n.ticks / 80f;
                ctx.fill(x - 2, y + 22, x + (int)(162 * frac) - 2, y + 24, accentColor);
            }

            y += 30;
            shown++;
        }
    }
}
