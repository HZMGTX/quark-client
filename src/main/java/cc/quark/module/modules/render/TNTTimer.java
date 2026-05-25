package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class TNTTimer extends Module {

    private final DoubleSetting warnSeconds = register(new DoubleSetting(
            "Warn Below", "Show warning color when fuse is under this many seconds", 2.0, 0.5, 5.0));
    private final DoubleSetting maxRange = register(new DoubleSetting(
            "Max Range", "Only show TNT within this range", 32.0, 8.0, 64.0));

    public TNTTimer() {
        super("TNTTimer", "Shows a countdown timer above active TNT entities", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack matrices = event.getMatrixStack();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof TntEntity tnt)) continue;
            if (mc.player.distanceTo(tnt) > maxRange.get()) continue;

            float fuse = tnt.getFuse() / 20f;
            boolean warn = fuse <= warnSeconds.get();

            Box box = tnt.getBoundingBox();
            float r = warn ? 1.0f : 1.0f;
            float g = warn ? 0.2f : 0.8f;
            float b = warn ? 0.2f : 0.0f;

            RenderUtil.drawESPBox(matrices, box, r, g, b, 0.9f, 1.5f);
            RenderUtil.drawFilledBox(matrices, box, r, g, b, warn ? 0.15f : 0.06f);
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof TntEntity tnt)) continue;
            if (mc.player.distanceTo(tnt) > maxRange.get()) continue;

            Vec3d pos = new Vec3d(tnt.getX(), tnt.getY() + tnt.getHeight() + 0.4, tnt.getZ());
            double[] screen = RenderUtil.project(pos);
            if (screen == null) continue;

            float fuse = tnt.getFuse() / 20f;
            String text = String.format("%.1fs", fuse);
            int color = fuse <= warnSeconds.get() ? 0xFFFF4444 : 0xFFFFCC00;
            int tx = (int) screen[0] - mc.textRenderer.getWidth(text) / 2;
            int ty = (int) screen[1];
            ctx.drawTextWithShadow(mc.textRenderer, text, tx, ty, color);
        }
    }
}
