package cc.quark.module.modules.staff;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.Deque;

public class LiveMonitor extends Module {

    private final IntSetting maxLines    = register(new IntSetting("MaxLines", "MaxLines", 8, 4, 20));
    private final BoolSetting showJoins  = register(new BoolSetting("ShowJoins", "ShowJoins", true));
    private final BoolSetting showChat   = register(new BoolSetting("ShowChat", "ShowChat", true));
    private final BoolSetting showFlags  = register(new BoolSetting("ShowFlags", "ShowFlags", true));
    private final IntSetting posX        = register(new IntSetting("X", "X", 4, 0, 1000));
    private final IntSetting posY        = register(new IntSetting("Y", "Y", 100, 0, 800));

    private final Deque<String> events = new ArrayDeque<>();

    public LiveMonitor() {
        super("LiveMonitor", "Shows a live feed of server events: joins, chat, flags", Category.STAFF);
    }


    public void addEvent(String event) {
        if (events.size() >= maxLines.get()) events.pollFirst();
        events.addLast(event);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage();
        if (msg == null) return;

        if (showJoins.isEnabled() && (msg.contains("joined the game") || msg.contains("left the game"))) {
            addEvent("§a↔ " + msg.substring(0, Math.min(50, msg.length())));
        } else if (showChat.isEnabled() && !msg.startsWith("[") && msg.contains(":")) {
            addEvent("§f● " + msg.substring(0, Math.min(50, msg.length())));
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        
        if (mc == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int x = posX.get(), y = posY.get();

        ctx.drawTextWithShadow(mc.textRenderer, "§b[LiveMonitor]", x, y, 0xFFFFFFFF);
        y += 11;

        for (String e : events) {
            ctx.drawTextWithShadow(mc.textRenderer, e, x, y, 0xFFCCCCCC);
            y += 10;
        }
    }
}
