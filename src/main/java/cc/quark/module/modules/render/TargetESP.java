package cc.quark.module.modules.render;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.modules.combat.KillAura;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

public class TargetESP extends Module {

    private final ColorSetting color = register(new ColorSetting(
            "Color", "ESP color for KillAura target", 0xFFFF4500));
    private final DoubleSetting lineWidth = register(new DoubleSetting(
            "Line Width", "Outline thickness", 2.0, 0.5, 5.0));
    private final DoubleSetting fillAlpha = register(new DoubleSetting(
            "Fill Alpha", "Fill transparency (0-255)", 40, 0, 255));
    private final DoubleSetting pulseSpeed = register(new DoubleSetting(
            "Pulse Speed", "Alpha pulse speed multiplier", 1.0, 0.1, 5.0));

    public TargetESP() {
        super("TargetESP", "Draws an ESP box around KillAura's current target", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        KillAura ka = Quark.getInstance().getModuleManager().getModule(KillAura.class);
        if (ka == null || !ka.isEnabled()) return;

        Entity target = ka.getTarget();
        if (target == null || target.isDead()) return;

        float tickDelta = event.getTickDelta();
        double ex = target.prevX + (target.getX() - target.prevX) * tickDelta;
        double ey = target.prevY + (target.getY() - target.prevY) * tickDelta;
        double ez = target.prevZ + (target.getZ() - target.prevZ) * tickDelta;

        Box box = target.getBoundingBox().offset(
                ex - target.getX(), ey - target.getY(), ez - target.getZ());

        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();

        long ms = System.currentTimeMillis();
        float pulse = (float)(Math.sin(ms * 0.002 * pulseSpeed.get()) * 0.5 + 0.5);
        float fa = (float)(fillAlpha.get() / 255.0) * pulse;

        Box expanded = box.expand(0.05);
        RenderUtil.drawESPBox(event.getMatrixStack(), expanded, r, g, b, 0.95f, (float) lineWidth.get() + 1.0f);
        RenderUtil.drawESPBox(event.getMatrixStack(), box, r, g, b, 1.0f, (float) lineWidth.get());
        if (fa > 0) {
            RenderUtil.drawFilledBox(event.getMatrixStack(), box, r, g, b, fa);
        }
    }
}
