package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class HealthBar extends Module {

    private final BoolSetting showAbsorption = register(new BoolSetting("ShowAbsorption", "Include absorption hearts in the bar", true));

    public HealthBar() {
        super("HealthBar", "Renders a health bar below each entity's nameplate", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity living)) continue;
            if (e == mc.player) continue;
            if (living.isDead()) continue;

            Vec3d headPos = new Vec3d(e.getX(), e.getY() + e.getHeight() + 0.3, e.getZ());
            double[] screen = RenderUtil.project(headPos);
            if (screen == null) continue;

            float maxHp  = living.getMaxHealth();
            float hp     = living.getHealth();
            float absorb = showAbsorption.isEnabled() ? living.getAbsorptionAmount() : 0f;
            float total  = hp + absorb;
            float pct    = maxHp > 0 ? Math.min(total / maxHp, 1f) : 1f;

            int barW = 40;
            int barH = 3;
            int bx   = (int) screen[0] - barW / 2;
            int by   = (int) screen[1] + 2;

            ctx.fill(bx, by, bx + barW, by + barH, 0xFF333333);
            int fillW = (int) (barW * pct);
            int col   = pct > 0.5f ? 0xFF33FF33 : (pct > 0.25f ? 0xFFFFAA00 : 0xFFFF3333);
            ctx.fill(bx, by, bx + fillW, by + barH, col);

            if (absorb > 0 && showAbsorption.isEnabled()) {
                int absorpW = (int) (barW * Math.min(absorb / maxHp, 1f));
                ctx.fill(bx, by, bx + absorpW, by + barH, 0xFFFFDD44);
            }
        }
    }
}
