package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ColorUtil;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class HealthTags extends Module {

    private final BoolSetting onlyPlayers = register(new BoolSetting("Only Players", "Only show tags on players", true));
    private final DoubleSetting range = register(new DoubleSetting("Range", "Max range to show tags", 30.0, 5.0, 100.0));
    private final BoolSetting background = register(new BoolSetting("Background", "Draw background behind tag", true));

    public HealthTags() {
        super("HealthTags", "Renders health percentage tags above entity heads in 2D", Category.COMBAT);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (onlyPlayers.isEnabled() && !(entity instanceof PlayerEntity)) continue;
            if (!(entity instanceof net.minecraft.entity.LivingEntity living)) continue;
            if (living.isRemoved() || living.getHealth() <= 0) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;

            Vec3d worldPos = new Vec3d(entity.getX(), entity.getY() + entity.getHeight() + 0.35, entity.getZ());
            double[] screen = RenderUtil.project(worldPos);
            if (screen == null) continue;

            float hp = living.getHealth();
            float maxHp = living.getMaxHealth();
            float pct = maxHp > 0 ? hp / maxHp : 1f;
            int color = ColorUtil.healthColor(pct) | 0xFF000000;

            String tag = String.format("%.1f", hp);
            int w = mc.textRenderer.getWidth(tag);
            int sx = (int) screen[0] - w / 2;
            int sy = (int) screen[1];

            if (background.isEnabled()) {
                ctx.fill(sx - 2, sy - 1, sx + w + 2, sy + mc.textRenderer.fontHeight + 1, 0x88000000);
            }
            ctx.drawText(mc.textRenderer, tag, sx, sy, color, true);
        }
    }
}
