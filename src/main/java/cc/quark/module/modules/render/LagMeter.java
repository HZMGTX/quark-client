package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class LagMeter extends Module {
    private final IntSetting x = register(new IntSetting("X", "HUD X", 10, 0, 1920));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y", 100, 0, 1080));

    private long lastTickTime = 0;
    private long tickDelta = 50;

    public LagMeter() {
        super("Lag Meter", "Shows estimated server lag/TPS", Category.RENDER, 0);
    }

    @EventHandler
    public void onTick(EventTick e) {
        long now = System.currentTimeMillis();
        if (lastTickTime != 0) tickDelta = now - lastTickTime;
        lastTickTime = now;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        double tps = Math.min(20.0, 1000.0 / Math.max(1, tickDelta));
        String color = tps >= 19 ? "§a" : tps >= 15 ? "§e" : "§c";
        ctx.drawText(mc.textRenderer, "TPS: " + color + String.format("%.1f", tps), x.get(), y.get(), 0xFFFFFF, true);
    }
}
