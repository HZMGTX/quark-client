package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import cc.quark.util.RenderUtil;

/**
 * DamageTag - renders each living entity's health as a 2D tag on screen.
 */
public class DamageTag extends Module {

    private final ColorSetting color = register(new ColorSetting("Color", "Tag color", 0xFFFF5555));

    public DamageTag() {
        super("DamageTag", "Renders health tags above entities", Category.COMBAT);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null || mc.textRenderer == null) return;
        DrawContext ctx = event.getDrawContext();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            Vec3d pos = new Vec3d(entity.getX(), entity.getY() + entity.getHeight() + 0.5, entity.getZ());
            double[] screen = RenderUtil.project(pos);
            if (screen == null) continue;
            String text = String.format("%.1f", living.getHealth());
            ctx.drawText(mc.textRenderer, text, (int) screen[0], (int) screen[1], color.get(), true);
        }
    }
}
