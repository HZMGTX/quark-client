package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.util.math.Box;

public class BeeESP extends Module {

    private final ColorSetting angryColor   = register(new ColorSetting("AngryColor",   "Color for angry bees",   0xFFFF4400));
    private final ColorSetting peacefulColor = register(new ColorSetting("PeacefulColor", "Color for calm bees", 0xFFFFDD00));

    public BeeESP() {
        super("BeeESP", "Highlights bees, using distinct colors for angry vs peaceful swarms", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof BeeEntity bee)) continue;
            boolean angry = bee.isAngry();
            ColorSetting cs = angry ? angryColor : peacefulColor;
            float r = cs.getRedF(), g = cs.getGreenF(), b = cs.getBlueF(), a = cs.getAlphaF();
            double ex = bee.prevX + (bee.getX() - bee.prevX) * td;
            double ey = bee.prevY + (bee.getY() - bee.prevY) * td;
            double ez = bee.prevZ + (bee.getZ() - bee.prevZ) * td;
            Box box = bee.getBoundingBox().offset(ex - bee.getX(), ey - bee.getY(), ez - bee.getZ());
            RenderUtil.drawESPBox(m, box, r, g, b, a, 1.2f);
            RenderUtil.drawFilledBox(m, box, r, g, b, 0.12f);
        }
    }
}
