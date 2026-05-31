package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Box;

public class FireworkESP extends Module {

    private final ColorSetting color = register(new ColorSetting("Color", "ESP color for firework rockets", 0xFFFF66FF));

    public FireworkESP() {
        super("FireworkESP", "Highlights firework rockets in-flight; useful for detecting elytra players", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof FireworkRocketEntity)) continue;

            double ex = e.prevX + (e.getX() - e.prevX) * td;
            double ey = e.prevY + (e.getY() - e.prevY) * td;
            double ez = e.prevZ + (e.getZ() - e.prevZ) * td;
            Box box = e.getBoundingBox().offset(ex - e.getX(), ey - e.getY(), ez - e.getZ()).expand(0.2);
            RenderUtil.drawESPBox(m, box, color.getRedF(), color.getGreenF(), color.getBlueF(), color.getAlphaF(), 1.5f);
            RenderUtil.drawFilledBox(m, box, color.getRedF(), color.getGreenF(), color.getBlueF(), color.getAlphaF() * 0.2f);
        }
    }
}
