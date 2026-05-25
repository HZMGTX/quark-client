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
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.Box;

public class CrystalESP extends Module {

    private final ColorSetting color = register(new ColorSetting(
            "Color", "ESP color for end crystals", 0xFFFF00FF));
    private final DoubleSetting fillAlpha = register(new DoubleSetting(
            "Fill Alpha", "Fill transparency (0–255)", 30, 0, 255));
    private final DoubleSetting lineWidth = register(new DoubleSetting(
            "Line Width", "Outline thickness", 1.5, 0.5, 4.0));

    public CrystalESP() {
        super("CrystalESP", "Draws ESP boxes around End Crystals in the world", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices = event.getMatrixStack();
        float r = color.getRedF(), g = color.getGreenF(), b = color.getBlueF();
        float fa = (float) (fillAlpha.get() / 255.0);
        float tickDelta = event.getTickDelta();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity)) continue;

            double ex = entity.prevX + (entity.getX() - entity.prevX) * tickDelta;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * tickDelta;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta;
            Box box = entity.getBoundingBox().offset(
                    ex - entity.getX(), ey - entity.getY(), ez - entity.getZ());

            RenderUtil.drawESPBox(matrices, box, r, g, b, 0.9f, (float) lineWidth.get());
            if (fa > 0) RenderUtil.drawFilledBox(matrices, box, r, g, b, fa);
        }
    }
}
