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
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ItemESP extends Module {

    private final IntSetting    range     = register(new IntSetting("Range", "ESP range in blocks", 32, 4, 128));
    private final BoolSetting   tracers   = register(new BoolSetting("Tracers", "Draw tracer lines to items", false));
    private final BoolSetting   fill      = register(new BoolSetting("Fill", "Fill the ESP box", true));
    private final ColorSetting  color     = register(new ColorSetting("Color", "ESP box color", 0xFFFFFF55));
    private final DoubleSetting fillAlpha = register(new DoubleSetting("Fill Alpha", "Fill transparency", 0.12, 0.0, 1.0));

    public ItemESP() {
        super("ItemESP", "Draws ESP boxes around dropped items in the world", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;

        MatrixStack matrices = event.getMatrixStack();
        float td = event.getTickDelta();
        Vec3d from = mc.player.getCameraPosVec(td);
        double maxRange = range.get();

        float r = color.getRedF(), g = color.getGreenF(), b = color.getBlueF(), a = color.getAlphaF();
        float fa = (float) fillAlpha.get();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity)) continue;
            if (mc.player.distanceTo(entity) > maxRange) continue;

            double ex = entity.prevX + (entity.getX() - entity.prevX) * td;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * td;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * td;
            Box box = entity.getBoundingBox().offset(ex - entity.getX(), ey - entity.getY(), ez - entity.getZ());

            RenderUtil.drawESPBox(matrices, box, r, g, b, a, 1.5f);

            if (fill.isEnabled() && fa > 0) {
                RenderUtil.drawFilledBox(matrices, box, r, g, b, fa);
            }

            if (tracers.isEnabled()) {
                Vec3d center = new Vec3d(ex, ey + entity.getHeight() / 2.0, ez);
                RenderUtil.drawLine3D(matrices, from, center, r, g, b, a * 0.8f, 1.0f);
            }
        }
    }
}
