package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class TpsDisplay extends Module {

    private final BoolSetting showMs = register(new BoolSetting("ShowMs", "Also show milliseconds per tick", false));
    private final IntSetting x = register(new IntSetting("X", "HUD X position", 4, 0, 3840));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 14, 0, 2160));

    private long lastPacketMs = -1;
    private double tps = 20.0;
    private double mspt = 50.0;

    public TpsDisplay() {
        super("TpsDisplay", "Estimates and displays server TPS (ticks per second)", Category.RENDER);
    }

    @EventHandler
    public void onPacket(EventPacketReceive event) {
        if (!(event.getPacket() instanceof WorldTimeUpdateS2CPacket)) return;
        long now = System.currentTimeMillis();
        if (lastPacketMs != -1) {
            long diff = now - lastPacketMs;
            double rawTps = 20000.0 / diff;
            tps  = Math.min(20.0, rawTps * 0.1 + tps * 0.9);
            mspt = (diff / 20.0) * 0.1 + mspt * 0.9;
        }
        lastPacketMs = now;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        int textColor;
        if      (tps >= 19.0) textColor = 0xFF55FF55;
        else if (tps >= 15.0) textColor = 0xFFFFFF55;
        else if (tps >= 10.0) textColor = 0xFFFF9944;
        else                  textColor = 0xFFFF5555;

        String text = String.format("TPS: %.1f", tps);
        if (showMs.isEnabled()) text += String.format("  %.0fms", mspt);

        ctx.drawTextWithShadow(mc.textRenderer, text, x.get(), y.get(), textColor);
    }
}
