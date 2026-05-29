package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

/**
 * MotionBlur - applies a simple screen-space motion blur overlay.
 *
 * When {@code onlyMoving} is enabled the overlay only appears when the player
 * is travelling faster than a small threshold. Otherwise a constant blur is
 * applied proportional to the {@code intensity} setting.
 */
public class MotionBlur extends Module {

    private final DoubleSetting intensity = register(new DoubleSetting(
            "Intensity", "Blur strength (0.0 = none, 0.9 = heavy)", 0.5, 0.0, 0.9));

    private final BoolSetting onlyMoving = register(new BoolSetting(
            "Only Moving", "Only blur when moving fast", true));

    public MotionBlur() {
        super("MotionBlur", "Applies a screen-space motion blur effect", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.getWindow() == null) return;

        double blurAmount = intensity.get();

        if (onlyMoving.isEnabled()) {
            Vec3d vel = mc.player.getVelocity();
            double speed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
            // Scale blur by horizontal speed; threshold ~0.2 blocks/tick
            if (speed < 0.05) return;
            blurAmount = Math.min(blurAmount, blurAmount * (speed / 0.4));
        }

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        int alpha = (int)(blurAmount * 160);
        if (alpha <= 0) return;
        int color = (alpha << 24); // black overlay with computed alpha
        ctx.fill(0, 0, sw, sh, color);
    }
}
