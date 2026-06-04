package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class TPSDisplay extends Module {

    private final IntSetting  x     = register(new IntSetting ("X", "HUD X", 4, 0, 3840));
    private final IntSetting  y     = register(new IntSetting ("Y", "HUD Y", 14, 0, 2160));
    private final BoolSetting color = register(new BoolSetting("Color", "Color-code by TPS quality", true));

    private long lastTime = -1;
    private double tps = 20.0;

    public TPSDisplay() {
        super("TPSDisplay", "Shows server TPS (ticks per second) on HUD", Category.RENDER);
    }

    @EventHandler
    public void onPacket(EventPacketReceive event) {
        if (!(event.getPacket() instanceof WorldTimeUpdateS2CPacket)) return;
        long now = System.currentTimeMillis();
        if (lastTime != -1) {
            long diff = now - lastTime;
            double rawTps = 20000.0 / diff;
            tps = Math.min(20.0, rawTps * 0.1 + tps * 0.9);
        }
        lastTime = now;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        String text = String.format("TPS: %.1f", tps);
        int clr = 0xFFFFFFFF;
        if (color.isEnabled()) {
            if      (tps >= 19.0) clr = 0xFF55FF55;
            else if (tps >= 15.0) clr = 0xFFFFFF55;
            else if (tps >= 10.0) clr = 0xFFFF9944;
            else                  clr = 0xFFFF5555;
        }
        ctx.drawTextWithShadow(mc.textRenderer, text, x.get(), y.get(), clr);
    }
}
