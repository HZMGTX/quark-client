package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.gui.ClickGUI;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ColorUtil;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationOverlay extends Module {

    public enum NotifType { SUCCESS, ERROR, INFO, WARNING }

    public static class Notification {
        public final String title, message;
        public final NotifType type;
        public int ticks;
        public float slideX; // current slide offset; starts at +200, animates to 0

        public Notification(String title, String message, NotifType type, int ticks) {
            this.title   = title;
            this.message = message;
            this.type    = type;
            this.ticks   = ticks;
            this.slideX  = 200f; // starts off-screen to the right
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
        int screenH = mc.getWindow().getScaledHeight();

        float delta = event.getTickDelta();

        // remove expired, animate remaining
        QUEUE.removeIf(n -> n.ticks <= 0);

        int shown = 0;
        int notifW = 170;
        int notifH = 30;
        int padding = 4;

        // stack upward from bottom-right
        // compute total height needed so we can start from bottom
        int visibleCount = Math.min(QUEUE.size(), maxShown.get());
        int baseY = screenH - padding - notifH;

        Iterator<Notification> it = QUEUE.iterator();
        while (it.hasNext() && shown < maxShown.get()) {
            Notification n = it.next();
            n.ticks--;
            if (n.ticks <= 0) { it.remove(); continue; }

            // animate slide-in: lerp slideX toward 0
            n.slideX += (0f - n.slideX) * Math.min(1f, delta * 0.25f);
            if (n.slideX < 0.5f) n.slideX = 0f;

            // fade-out in last 20 ticks
            float alpha = n.ticks < 20 ? n.ticks / 20f : 1f;
            // also fade in during first 10 ticks (based on 80 total)
            if (n.ticks > 60) alpha = Math.min(alpha, (80 - n.ticks) / 20f);
            int bgAlpha = (int)(180 * alpha);
            int fgAlpha = (int)(255 * alpha);

            int accentRgb = switch (n.type) {
                case SUCCESS -> 0x44FF44;
                case ERROR   -> 0xFF4444;
                case WARNING -> 0xFFAA00;
                case INFO    -> 0x4488FF;
            };

            String icon = switch (n.type) {
                case SUCCESS -> "✓"; // ✓
                case ERROR   -> "✗"; // ✗
                case WARNING -> "⚠"; // ⚠
                case INFO    -> "ℹ"; // ℹ
            };

            int yPos  = baseY - shown * (notifH + padding);
            int xPos  = screenW - notifW - padding + (int)n.slideX;

            // dark background
            ctx.fill(xPos, yPos, xPos + notifW, yPos + notifH,
                     ColorUtil.withAlpha(0x111111, bgAlpha));
            // left accent bar (3px)
            ctx.fill(xPos, yPos, xPos + 3, yPos + notifH,
                     ColorUtil.withAlpha(accentRgb, fgAlpha));
            // top border line (1px)
            ctx.fill(xPos, yPos, xPos + notifW, yPos + 1,
                     ColorUtil.withAlpha(accentRgb, (int)(100 * alpha)));

            int accentColor = ColorUtil.withAlpha(accentRgb, fgAlpha);
            int titleColor  = accentColor; // title in accent color
            int msgColor    = ColorUtil.withAlpha(0xFFFFFF, fgAlpha);
            int dimColor    = ColorUtil.withAlpha(0xAAAAAA, fgAlpha);

            // icon + title on first line
            RenderUtil.drawCustomText(ctx, icon + " " + n.title, xPos + 7, yPos + 4, titleColor);
            // message on second line
            RenderUtil.drawCustomText(ctx, n.message, xPos + 7, yPos + 15, msgColor);

            // timer bar at bottom
            if (timerBar.isEnabled()) {
                float frac = n.ticks / 80f;
                int barY = yPos + notifH - 2;
                ctx.fill(xPos + 3, barY, xPos + notifW, barY + 2,
                         ColorUtil.withAlpha(0x333333, bgAlpha));
                ctx.fill(xPos + 3, barY, xPos + 3 + (int)((notifW - 3) * frac), barY + 2,
                         accentColor);
            }

            shown++;
        }
    }
}
