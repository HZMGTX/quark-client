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

public class KillFeed extends Module {

    private final IntSetting  posX     = register(new IntSetting("X", "HUD X position", 10, 0, 3000));
    private final IntSetting  posY     = register(new IntSetting("Y", "HUD Y position", 40, 0, 3000));
    private final IntSetting  maxShown = register(new IntSetting("Max Shown", "Max kill messages on screen", 5, 1, 15));
    private final IntSetting  fadeMs   = register(new IntSetting("Fade Time", "Milliseconds before messages fade", 6000, 1000, 30000));
    private final BoolSetting highlight = register(new BoolSetting("Highlight Self", "Highlight kills involving you in gold", true));
    private final BoolSetting bgBox    = register(new BoolSetting("Background", "Draw background box", true));

    private static final String[] KILL_KEYWORDS = {
        "was slain by", "was killed by", "was shot by", "fell to their death",
        "drowned", "burned to death", "died", "was blown up", "was obliterated",
        "got finished off", "hit the ground", "fell out of the world", "starved"
    };

    private static class KillEntry {
        final String text;
        final boolean self;
        final TimerUtil timer = new TimerUtil();
        KillEntry(String text, boolean self) { this.text = text; this.self = self; timer.reset(); }
    }

    private final Deque<KillEntry> feed = new ArrayDeque<>();

    public KillFeed() {
        super("KillFeed", "Shows kill notifications like in FPS games", Category.RENDER);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
        feed.clear();
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        feed.clear();
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage();
        String lower = msg.toLowerCase();

        for (String kw : KILL_KEYWORDS) {
            if (lower.contains(kw)) {
                String name = mc.player != null ? mc.player.getName().getString().toLowerCase() : "";
                boolean self = !name.isEmpty() && lower.contains(name);
                feed.addFirst(new KillEntry(msg, self));
                while (feed.size() > maxShown.get()) feed.removeLast();
                return;
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        feed.removeIf(e -> e.timer.hasReached(fadeMs.get()));
        if (feed.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        List<KillEntry> visible = new ArrayList<>(feed);
        int count = Math.min(visible.size(), maxShown.get());
        int lh = mc.textRenderer.fontHeight + 2;
        int x = posX.get(), y = posY.get();

        if (bgBox.isEnabled()) {
            int maxW = 0;
            for (int i = 0; i < count; i++) maxW = Math.max(maxW, mc.textRenderer.getWidth(visible.get(i).text));
            ctx.fill(x - 2, y - 2, x + maxW + 2, y + count * lh + 1, 0x88000000);
        }

        for (int i = 0; i < count; i++) {
            KillEntry e = visible.get(i);
            int color = (highlight.isEnabled() && e.self) ? 0xFFFFAA00 : 0xFFFFFFFF;
            ctx.drawTextWithShadow(mc.textRenderer, e.text, x, y, color);
            y += lh;
        }
    }
}
