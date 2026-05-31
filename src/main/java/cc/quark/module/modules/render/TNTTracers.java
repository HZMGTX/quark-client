package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.Vec3d;

public class TNTTracers extends Module {

    private final ColorSetting color = register(new ColorSetting("Color", "Tracer line color", 0xFFFF4400));

    public TNTTracers() {
        super("TNTTracers", "Draws tracer lines to all primed TNT with time-to-explosion label", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float r = color.getRedF(), g = color.getGreenF(), b = color.getBlueF(), a = color.getAlphaF();
        Vec3d origin = mc.player.getCameraPosVec(event.getTickDelta());

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof TntEntity tnt)) continue;
            Vec3d target = new Vec3d(tnt.getX(), tnt.getY() + tnt.getHeight() * 0.5, tnt.getZ());
            RenderUtil.drawLine3D(m, origin, target, r, g, b, a, 1.0f);
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof TntEntity tnt)) continue;
            Vec3d pos = new Vec3d(tnt.getX(), tnt.getY() + tnt.getHeight() + 0.3, tnt.getZ());
            double[] screen = RenderUtil.project(pos);
            if (screen == null) continue;
            float secs = tnt.getFuse() / 20f;
            String label = String.format("%.1fs", secs);
            int col = secs <= 1f ? 0xFFFF2222 : secs <= 2.5f ? 0xFFFF8800 : 0xFFFFDD00;
            int tx = (int) screen[0] - mc.textRenderer.getWidth(label) / 2;
            ctx.drawTextWithShadow(mc.textRenderer, label, tx, (int) screen[1], col);
        }
    }
}
