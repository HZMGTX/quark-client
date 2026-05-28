package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.client.gui.DrawContext;

public class HealthVignette extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting(
            "Threshold", "Health percent below which the vignette activates (0-1)", 0.4, 0.0, 1.0));

    public HealthVignette() {
        super("HealthVignette", "Flashes a red screen vignette when health is critically low", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.getWindow() == null) return;

        float hp    = mc.player.getHealth();
        float maxHp = mc.player.getMaxHealth();
        if (maxHp <= 0) return;

        float pct = hp / maxHp;
        if (pct >= (float) threshold.get()) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        float danger = 1.0f - (pct / (float) threshold.get());
        float pulse = (float)(0.6 + 0.4 * Math.sin(System.currentTimeMillis() / 200.0));
        float alpha = danger * pulse;

        int steps = 50;
        for (int i = 0; i < steps; i++) {
            float t = (float) i / steps;
            int a = (int)(alpha * 160 * t * t);
            int col = (a << 24) | (0xFF << 16);
            ctx.fill(i, i, sw - i, i + 1, col);
            ctx.fill(i, sh - i - 1, sw - i, sh - i, col);
            ctx.fill(i, i, i + 1, sh - i, col);
            ctx.fill(sw - i - 1, i, sw - i, sh - i, col);
        }

        if (danger > 0.6f) {
            int centerAlpha = (int)(danger * pulse * 40);
            ctx.fill(0, 0, sw, sh, (centerAlpha << 24) | (0xFF << 16));
        }
    }
}
