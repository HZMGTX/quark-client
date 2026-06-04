package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class SessionTimer extends Module {

    private final IntSetting x = register(new IntSetting("X", "HUD X position", 4, 0, 3840));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 210, 0, 2160));

    private long startMs = -1;

    public SessionTimer() {
        super("SessionTimer", "Displays current session playtime on HUD", Category.MISC);
    }

    @Override
    public void onEnable() {
        startMs = System.currentTimeMillis();
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || startMs < 0) return;
        DrawContext ctx = event.getDrawContext();
        long elapsed = (System.currentTimeMillis() - startMs) / 1000;
        long h = elapsed / 3600, m = (elapsed % 3600) / 60, s = elapsed % 60;
        String text = String.format("Session: %02d:%02d:%02d", h, m, s);
        ctx.drawTextWithShadow(mc.textRenderer, text, x.get(), y.get(), 0xFFAAAAAA);
    }
}
