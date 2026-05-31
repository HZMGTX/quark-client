package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.Deque;

public class CombatOverlay extends Module {

    private final IntSetting maxLines = register(new IntSetting("MaxLines", "Maximum lines to display in the log", 8, 1, 20));

    // Public so external mixins can push events without depending on a singleton
    public static final Deque<String> LOG = new ArrayDeque<>();

    public CombatLog() {
        super("CombatOverlay", "Renders a log of recent combat events as a HUD overlay", Category.RENDER);
    }

    /** Call from damage/attack mixins to add a log line. */
    public static void addEntry(String line) {
        LOG.addFirst(line);
        while (LOG.size() > 32) LOG.removeLast();
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || LOG.isEmpty()) return;
        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int limit = maxLines.get();
        int y = 4;
        int idx = 0;
        for (String line : LOG) {
            if (idx >= limit) break;
            int x = sw - mc.textRenderer.getWidth(line) - 4;
            ctx.fill(x - 1, y - 1, sw - 3, y + 9, 0x80000000);
            RenderUtil.drawCustomText(ctx, line, x, y, 0xFFFFFFFF);
            y += 11;
            idx++;
        }
    }
}
