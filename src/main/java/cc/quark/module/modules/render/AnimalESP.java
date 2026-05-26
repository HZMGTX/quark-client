package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.Box;

public class AnimalESP extends Module {

    private final ColorSetting color = register(new ColorSetting("Color", "Box color", 0xFF40FF80));
    private final DoubleSetting fillAlpha = register(new DoubleSetting("Fill Alpha", "Fill transparency", 0.1, 0, 1));

    public AnimalESP() {
        super("AnimalESP", "Draws ESP boxes around animals", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof AnimalEntity)) continue;
            double ex = e.prevX + (e.getX() - e.prevX) * td;
            double ey = e.prevY + (e.getY() - e.prevY) * td;
            double ez = e.prevZ + (e.getZ() - e.prevZ) * td;
            Box box = e.getBoundingBox().offset(ex - e.getX(), ey - e.getY(), ez - e.getZ());
            RenderUtil.drawESPBox(m, box, color.getRedF(), color.getGreenF(), color.getBlueF(), color.getAlphaF(), 1.2f);
            float fa = (float) fillAlpha.get();
            if (fa > 0) RenderUtil.drawFilledBox(m, box, color.getRedF(), color.getGreenF(), color.getBlueF(), fa);
        }
    }
}
