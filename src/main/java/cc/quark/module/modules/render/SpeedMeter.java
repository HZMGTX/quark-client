package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

public class SpeedMeter extends Module {

    private final IntSetting x      = register(new IntSetting("X",   "HUD X position",           4, 0, 1920));
    private final IntSetting y      = register(new IntSetting("Y",   "HUD Y position",           100, 0, 1080));
    private final BoolSetting kmh   = register(new BoolSetting("KMH", "Display speed in km/h instead of BPS", false));

    public SpeedMeter() {
        super("SpeedMeter", "Shows current movement speed as HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        Vec3d vel  = mc.player.getVelocity();
        double bps = Math.sqrt(vel.x * vel.x + vel.z * vel.z) * 20.0;

        String label;
        if (kmh.isEnabled()) {
            double kmhVal = bps * 3.6;
            label = String.format("%.1f km/h", kmhVal);
        } else {
            label = String.format("%.2f BPS", bps);
        }

        int textColor;
        if (bps > 10.0)      textColor = 0xFFFF5555;
        else if (bps > 5.0)  textColor = 0xFFFFAA00;
        else if (bps > 2.0)  textColor = 0xFF55FF55;
        else                 textColor = 0xFFFFFFFF;

        ctx.drawTextWithShadow(mc.textRenderer, label, x.get(), y.get(), textColor);
    }
}
