package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

/**
 * LowHealthAlert - Draws a flashing red border around the screen when HP is low.
 *
 * The border pulses faster and becomes more opaque as health drops further.
 * Configurable threshold (as a fraction of max HP) and border thickness.
 */
public class LowHealthAlert extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting(
            "Threshold", "Health fraction below which the alert activates (0-1)", 0.35, 0.05, 1.0));
    private final IntSetting borderSize = register(new IntSetting(
            "Border",    "Thickness of the alert border in pixels",              30,   4,   80));
    private final IntSetting pulseSpeed = register(new IntSetting(
            "Speed",     "Pulse speed (ms per cycle, lower = faster)",           400, 100, 2000));

    public LowHealthAlert() {
        super("LowHealthAlert", "Flashes a red screen border when health is critically low", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        float hp    = mc.player.getHealth();
        float maxHp = mc.player.getMaxHealth();
        if (maxHp <= 0) return;

        float pct = hp / maxHp;
        float thresh = (float) threshold.get();
        if (pct >= thresh) return;

        DrawContext ctx = event.getDrawContext();
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        // How deep into the danger zone (0 = just crossed threshold, 1 = 0 HP)
        float danger = 1.0f - (pct / thresh);

        // Pulse: faster and brighter as danger increases
        long speed = Math.max(100L, pulseSpeed.get() - (long)(danger * (pulseSpeed.get() - 100)));
        float pulse = (float)(0.5 + 0.5 * Math.sin(System.currentTimeMillis() * Math.PI / speed));

        float alpha = danger * pulse * 0.85f;
        int a = Math.min(255, (int)(alpha * 255));
        int color = (a << 24) | 0xFF0000;

        int b = borderSize.get();

        // Top edge
        ctx.fill(0, 0, sw, b, color);
        // Bottom edge
        ctx.fill(0, sh - b, sw, sh, color);
        // Left edge
        ctx.fill(0, b, b, sh - b, color);
        // Right edge
        ctx.fill(sw - b, b, sw, sh - b, color);
    }
}
