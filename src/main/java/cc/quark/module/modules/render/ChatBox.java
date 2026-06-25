package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.Deque;

public class ChatBox extends Module {

    private final ColorSetting bgColor = register(new ColorSetting(
            "Background", "Chat background color (ARGB)", 0x88000000));

    private final IntSetting maxLines = register(new IntSetting(
            "Max Lines", "Maximum number of chat lines to display", 10, 1, 30));

    private final Deque<String> lines = new ArrayDeque<>();

    public ChatBox() {
        super("ChatBox", "Custom styled chat box replacement", Category.RENDER);
    }

    @Override
    public void onEnable() {
        lines.clear();
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage();
        if (msg == null || msg.isEmpty()) return;

        lines.addFirst(msg);
        while (lines.size() > maxLines.get()) {
            lines.removeLast();
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || lines.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        int lh = mc.textRenderer.fontHeight + 1;
        int count = Math.min(lines.size(), maxLines.get());
        int boxH = count * lh + 4;
        int boxW = 200;
        int boxX = 2;
        int boxY = sh - 48 - boxH;

        // Background
        ctx.fill(boxX, boxY, boxX + boxW, boxY + boxH, bgColor.get());

        // Draw lines top to bottom (newest at bottom)
        String[] lineArr = lines.toArray(new String[0]);
        for (int i = 0; i < count; i++) {
            int yi = boxY + 2 + (count - 1 - i) * lh;
            String line = lineArr[i];
            if (line.length() > 28) line = line.substring(0, 28) + "...";
            ctx.drawTextWithShadow(mc.textRenderer, line, boxX + 2, yi, 0xFFFFFFFF);
        }
    }
}
