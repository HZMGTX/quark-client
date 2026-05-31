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
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class DragonESP extends Module {

    private final BoolSetting showRange = register(new BoolSetting("ShowRange", "Visualize dragon attack range sphere", true));

    public DragonESP() {
        super("DragonESP", "Special ESP marker for the Ender Dragon with health and range display", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EnderDragonEntity dragon)) continue;
            double ex = dragon.prevX + (dragon.getX() - dragon.prevX) * td;
            double ey = dragon.prevY + (dragon.getY() - dragon.prevY) * td;
            double ez = dragon.prevZ + (dragon.getZ() - dragon.prevZ) * td;
            Box box = dragon.getBoundingBox().offset(ex - dragon.getX(), ey - dragon.getY(), ez - dragon.getZ());
            RenderUtil.drawESPBox(m, box, 0.6f, 0f, 0.8f, 0.9f, 2.0f);
            RenderUtil.drawFilledBox(m, box, 0.6f, 0f, 0.8f, 0.08f);

            if (showRange.isEnabled()) {
                Box rangeBox = new Box(ex - 10, ey - 10, ez - 10, ex + 10, ey + 10, ez + 10);
                RenderUtil.drawESPBox(m, rangeBox, 1f, 0f, 0f, 0.3f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EnderDragonEntity dragon)) continue;
            Vec3d pos = new Vec3d(dragon.getX(), dragon.getY() + dragon.getHeight() + 0.5, dragon.getZ());
            double[] screen = RenderUtil.project(pos);
            if (screen == null) continue;
            float hp = dragon.getHealth();
            float maxHp = dragon.getMaxHealth();
            String label = String.format("Dragon  %.0f/%.0f", hp, maxHp);
            int tx = (int) screen[0] - mc.textRenderer.getWidth(label) / 2;
            ctx.drawTextWithShadow(mc.textRenderer, label, tx, (int) screen[1], 0xFFCC44FF);
        }
    }
}
