package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class WitherESP extends Module {

    private final BoolSetting showSkulls = register(new BoolSetting("ShowSkulls", "Highlight wither skull projectiles", true));

    public WitherESP() {
        super("WitherESP", "ESP for wither boss with health bar and skull trajectory highlights", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof WitherEntity wither) {
                double ex = wither.prevX + (wither.getX() - wither.prevX) * td;
                double ey = wither.prevY + (wither.getY() - wither.prevY) * td;
                double ez = wither.prevZ + (wither.getZ() - wither.prevZ) * td;
                Box box = wither.getBoundingBox().offset(ex - wither.getX(), ey - wither.getY(), ez - wither.getZ());
                RenderUtil.drawESPBox(m, box, 0.15f, 0.15f, 0.15f, 0.9f, 2.0f);
                RenderUtil.drawFilledBox(m, box, 0.15f, 0.15f, 0.15f, 0.1f);
            } else if (showSkulls.isEnabled() && entity instanceof WitherSkullEntity skull) {
                double sx = skull.prevX + (skull.getX() - skull.prevX) * td;
                double sy = skull.prevY + (skull.getY() - skull.prevY) * td;
                double sz = skull.prevZ + (skull.getZ() - skull.prevZ) * td;
                Box sBox = skull.getBoundingBox().offset(sx - skull.getX(), sy - skull.getY(), sz - skull.getZ());
                RenderUtil.drawESPBox(m, sBox, 0.5f, 0f, 0f, 0.9f, 1.5f);
                RenderUtil.drawFilledBox(m, sBox, 0.5f, 0f, 0f, 0.2f);
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof WitherEntity wither)) continue;
            Vec3d pos = new Vec3d(wither.getX(), wither.getY() + wither.getHeight() + 0.5, wither.getZ());
            double[] screen = RenderUtil.project(pos);
            if (screen == null) continue;
            float hp = wither.getHealth();
            float maxHp = wither.getMaxHealth();
            float pct = maxHp > 0 ? hp / maxHp : 1f;
            String label = String.format("Wither  %.0f/%.0f", hp, maxHp);
            int tx = (int) screen[0] - mc.textRenderer.getWidth(label) / 2;
            int ty = (int) screen[1];
            ctx.drawTextWithShadow(mc.textRenderer, label, tx, ty, 0xFFAAAAAA);
            int barW = 60;
            int filled = (int) (barW * pct);
            ctx.fill(tx, ty + 10, tx + barW, ty + 13, 0xFF333333);
            int barColor = pct > 0.5f ? 0xFF55FF55 : pct > 0.25f ? 0xFFFFFF55 : 0xFFFF5555;
            ctx.fill(tx, ty + 10, tx + filled, ty + 13, barColor);
        }
    }
}
