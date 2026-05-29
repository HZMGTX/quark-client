package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class AnimalESP extends Module {

    private final ColorSetting  color     = register(new ColorSetting("Color", "Box color", 0xFF40FF80));
    private final DoubleSetting fillAlpha = register(new DoubleSetting("Fill Alpha", "Fill transparency", 0.1, 0.0, 1.0));
    private final IntSetting    range     = register(new IntSetting("Range", "Max render range in blocks", 64, 8, 256));
    private final BoolSetting   tracers   = register(new BoolSetting("Tracers", "Draw tracer lines to animals", false));

    public AnimalESP() {
        super("AnimalESP", "Draws ESP boxes around animals", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();
        Vec3d from = mc.player.getCameraPosVec(td);
        double maxRange = range.get();

        float r = color.getRedF(), g = color.getGreenF(), b = color.getBlueF(), a = color.getAlphaF();
        float fa = (float) fillAlpha.get();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof AnimalEntity)) continue;
            if (mc.player.distanceTo(e) > maxRange) continue;

            double ex = e.prevX + (e.getX() - e.prevX) * td;
            double ey = e.prevY + (e.getY() - e.prevY) * td;
            double ez = e.prevZ + (e.getZ() - e.prevZ) * td;
            Box box = e.getBoundingBox().offset(ex - e.getX(), ey - e.getY(), ez - e.getZ());

            RenderUtil.drawESPBox(m, box, r, g, b, a, 1.2f);
            if (fa > 0) RenderUtil.drawFilledBox(m, box, r, g, b, fa);

            if (tracers.isEnabled()) {
                Vec3d center = new Vec3d(ex, ey + e.getHeight() / 2.0, ez);
                RenderUtil.drawLine3D(m, from, center, r, g, b, a * 0.7f, 1.0f);
            }
        }
    }
}
