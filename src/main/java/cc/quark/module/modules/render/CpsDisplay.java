package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.Deque;

public class CpsDisplay extends Module {

    private final IntSetting x = register(new IntSetting("X", "HUD X position", 4, 0, 3840));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 24, 0, 2160));
    private final ColorSetting color = register(new ColorSetting("Color", "Text color", 0xFFFFFFFF));

    // Timestamps of recent left-clicks (within last 1 second)
    private final Deque<Long> clickTimes = new ArrayDeque<>();
    private int cps = 0;

    public CpsDisplay() {
        super("CpsDisplay", "Shows current clicks-per-second on the HUD", Category.RENDER);
    }

    /** Call this from a mouse-click mixin or the attack event to register a click. */
    public void registerClick() {
        clickTimes.addLast(System.currentTimeMillis());
    }

    @EventHandler
    public void onTick(EventTick event) {
        long now = System.currentTimeMillis();
        // Prune clicks older than 1 second
        while (!clickTimes.isEmpty() && now - clickTimes.peekFirst() > 1000) {
            clickTimes.pollFirst();
        }
        cps = clickTimes.size();
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        String text = "CPS: " + cps;
        ctx.drawTextWithShadow(mc.textRenderer, text, x.get(), y.get(), color.get());
    }
}
