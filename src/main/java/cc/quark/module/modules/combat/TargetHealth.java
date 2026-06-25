package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.EntityUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class TargetHealth extends Module {

    private final BoolSetting showName = register(new BoolSetting("Show Name", "Show entity name above health bar", true));
    private final BoolSetting showDistance = register(new BoolSetting("Show Distance", "Show distance to target", true));

    public TargetHealth() {
        super("TargetHealth", "Shows targeted entity health bar above screen", Category.COMBAT);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        HitResult crosshairTarget = mc.crosshairTarget;
        if (crosshairTarget == null || crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        if (!(crosshairTarget instanceof EntityHitResult entityHit)) return;
        if (!(entityHit.getEntity() instanceof LivingEntity target)) return;

        DrawContext ctx = event.getDrawContext();
        int screenW = mc.getWindow().getScaledWidth();

        float hp = target.getHealth();
        float maxHp = target.getMaxHealth();
        float pct = Math.max(0f, Math.min(1f, hp / maxHp));

        int barWidth = 120;
        int barHeight = 8;
        int x = (screenW - barWidth) / 2;
        int y = 8;

        // Background
        ctx.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xAA000000);

        // Health bar color: green -> yellow -> red
        int barColor;
        if (pct > 0.5f) {
            int g = (int) (255 * ((pct - 0.5f) * 2f));
            barColor = 0xFF00FF00 | (g << 8);
            barColor = 0xFF000000 | (255 << 8) | (g << 16);
        } else {
            int r = (int) (255 * (pct * 2f));
            barColor = 0xFF000000 | 0xFF0000 | (r << 8);
        }
        // Simpler color blend
        int red = pct < 0.5f ? 255 : (int) (255 * (1f - pct) * 2);
        int green = pct > 0.5f ? 255 : (int) (255 * pct * 2);
        barColor = 0xFF000000 | (red << 16) | (green << 8);

        ctx.fill(x, y, x + (int) (barWidth * pct), y + barHeight, barColor);
        ctx.fill(x + (int) (barWidth * pct), y, x + barWidth, y + barHeight, 0xFF555555);

        // HP text inside bar
        String hpText = String.format("%.1f / %.1f", hp, maxHp);
        ctx.drawTextWithShadow(mc.textRenderer, hpText,
                x + barWidth / 2 - mc.textRenderer.getWidth(hpText) / 2,
                y + 1, 0xFFFFFFFF);

        int textY = y - 10;
        if (showName.isEnabled()) {
            String name = target.getName().getString();
            ctx.drawTextWithShadow(mc.textRenderer, name,
                    x + barWidth / 2 - mc.textRenderer.getWidth(name) / 2,
                    textY, 0xFFFFFFFF);
            textY -= 10;
        }

        if (showDistance.isEnabled()) {
            double dist = EntityUtil.distanceTo(target);
            String distText = String.format("%.1fm", dist);
            ctx.drawTextWithShadow(mc.textRenderer, distText,
                    x + barWidth / 2 - mc.textRenderer.getWidth(distText) / 2,
                    textY, 0xFFAAAAAA);
        }
    }
}
