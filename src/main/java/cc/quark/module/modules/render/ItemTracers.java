package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Vec3d;

public class ItemTracers extends Module {

    private final ColorSetting color = register(new ColorSetting("Color", "Tracer color", 0xFFFFFF40));

    public ItemTracers() {
        super("ItemTracers", "Draws tracer lines to dropped items", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();
        Vec3d from = mc.player.getCameraPosVec(td);
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof ItemEntity)) continue;
            double ex = e.prevX + (e.getX() - e.prevX) * td;
            double ey = e.prevY + (e.getY() - e.prevY) * td;
            double ez = e.prevZ + (e.getZ() - e.prevZ) * td;
            RenderUtil.drawLine3D(m, from, new Vec3d(ex, ey, ez),
                    color.getRedF(), color.getGreenF(), color.getBlueF(), color.getAlphaF(), 1.0f);
        }
    }
}
