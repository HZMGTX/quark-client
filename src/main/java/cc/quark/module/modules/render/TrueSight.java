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
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;

public class TrueSight extends Module {

    private final ColorSetting color = register(new ColorSetting(
            "Color", "ESP color for invisible entities", 0xFF00FFFF));
    private final DoubleSetting fillAlpha = register(new DoubleSetting(
            "Fill Alpha", "Box fill transparency (0–255)", 25, 0, 255));

    public TrueSight() {
        super("TrueSight", "Draws ESP around invisible and glowing entities", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices  = event.getMatrixStack();
        float       tickDelta = event.getTickDelta();
        float r = color.getRedF(), g = color.getGreenF(), b = color.getBlueF();
        float fa = (float) (fillAlpha.get() / 255.0);

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!entity.isInvisible() && !(entity instanceof LivingEntity le && le.isGlowing())) continue;

            double ex = entity.prevX + (entity.getX() - entity.prevX) * tickDelta;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * tickDelta;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta;
            Box box = entity.getBoundingBox().offset(
                    ex - entity.getX(), ey - entity.getY(), ez - entity.getZ());

            RenderUtil.drawESPBox(matrices, box, r, g, b, 0.85f, 1.5f);
            RenderUtil.drawFilledBox(matrices, box, r, g, b, fa);
        }
    }
}
