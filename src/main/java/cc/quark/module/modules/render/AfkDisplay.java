package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

/**
 * AfkDisplay - Shows an AFK status overlay with time elapsed since last input.
 */
public class AfkDisplay extends Module {

    private final IntSetting  x         = register(new IntSetting ("X",         "HUD X position",                          4,    0, 1000));
    private final IntSetting  y         = register(new IntSetting ("Y",         "HUD Y position",                          40,   0, 600));
    private final IntSetting  threshold = register(new IntSetting ("Threshold", "Seconds of inactivity to show AFK label", 30,   5, 300));
    private final BoolSetting showTime  = register(new BoolSetting("Show Time", "Display elapsed AFK duration",             true));
    private final ColorSetting color    = register(new ColorSetting("Color",    "AFK label color",                          0xFFFF5555));

    private long lastInputTime  = System.currentTimeMillis();
    private Vec3d lastPos       = null;
    private float lastYaw       = 0f;

    public AfkDisplay() {
        super("AfkDisplay", "Shows AFK status overlay and time spent AFK", Category.RENDER);
    }

    @Override
    public void onEnable() {
        lastInputTime = System.currentTimeMillis();
        lastPos = null;
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        Vec3d pos = mc.player.getPos();
        float yaw = mc.player.getYaw();

        boolean moved = lastPos != null && (pos.distanceTo(lastPos) > 0.01 || Math.abs(yaw - lastYaw) > 0.5f);
        if (moved) {
            lastInputTime = System.currentTimeMillis();
        }
        lastPos = pos;
        lastYaw = yaw;
    }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.player == null) return;
        long elapsed = System.currentTimeMillis() - lastInputTime;
        if (elapsed < threshold.get() * 1000L) return;

        DrawContext ctx = e.getDrawContext();
        long secs = elapsed / 1000L;
        String label = showTime.isEnabled()
                ? "AFK - " + formatTime(secs)
                : "AFK";
        ctx.drawTextWithShadow(mc.textRenderer, label, x.get(), y.get(), color.get());
    }

    private String formatTime(long totalSecs) {
        long h = totalSecs / 3600;
        long m = (totalSecs % 3600) / 60;
        long s = totalSecs % 60;
        if (h > 0) return String.format("%dh %02dm %02ds", h, m, s);
        if (m > 0) return String.format("%dm %02ds", m, s);
        return totalSecs + "s";
    }
}
