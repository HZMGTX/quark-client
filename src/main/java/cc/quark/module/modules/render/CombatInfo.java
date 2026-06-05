package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class CombatInfo extends Module {

    private LivingEntity target;
    private int hitsLanded;
    private float lastDamageDealt;
    private float targetPrevHealth;
    private long lastAttackTime;
    private long lastHitTime;

    public CombatInfo() {
        super("CombatInfo", "HUD overlay showing combat stats: target, health, hits, damage", Category.RENDER);
    }

    @Override
    public void onEnable() {
        target = null;
        hitsLanded = 0;
        lastDamageDealt = 0f;
        targetPrevHealth = 0f;
        lastAttackTime = 0;
        lastHitTime = 0;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (event.getTarget() instanceof LivingEntity le) {
            if (target != le) {
                target = le;
                hitsLanded = 0;
                lastDamageDealt = 0f;
                targetPrevHealth = le.getHealth();
            }
            lastAttackTime = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (target == null) return;

        if (System.currentTimeMillis() - lastAttackTime > 5000 || !target.isAlive() || target.isRemoved()) {
            target = null;
            return;
        }

        float currentHealth = target.getHealth();
        if (currentHealth < targetPrevHealth) {
            float dmg = targetPrevHealth - currentHealth;
            if (dmg > 0.05f) {
                lastDamageDealt = dmg;
                hitsLanded++;
                lastHitTime = System.currentTimeMillis();
            }
        }
        targetPrevHealth = currentHealth;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        if (target == null || !target.isAlive()) {
            if (target != null && hitsLanded > 0) {
                DrawContext ctx = event.getDrawContext();
                int x = mc.getWindow().getScaledWidth() - 130;
                ctx.fill(x - 4, 3, mc.getWindow().getScaledWidth() - 2, 28, 0xAA181818);
                ctx.fill(x - 4, 3, mc.getWindow().getScaledWidth() - 2, 4, 0xFFFF4444);
                ctx.drawTextWithShadow(mc.textRenderer, "Target Dead", x, 7, 0xFFFF4444);
                ctx.drawTextWithShadow(mc.textRenderer, "Hits: " + hitsLanded, x, 17, 0xFFAAAAAA);
            }
            return;
        }

        DrawContext ctx = event.getDrawContext();
        int screenW = mc.getWindow().getScaledWidth();
        int x = screenW - 130;
        int y = 3;
        int panelW = 128;
        int panelH = 58;

        ctx.fill(x - 4, y, x - 4 + panelW, y + panelH, 0xAA181818);
        ctx.fill(x - 4, y, x - 4 + panelW, y + 1, 0xFFFF4444);

        String name = target.getName().getString();
        if (name.length() > 14) name = name.substring(0, 13) + "..";
        ctx.drawTextWithShadow(mc.textRenderer, name, x, y + 4, 0xFFFFFFFF);

        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        float healthPct = MathHelper.clamp(health / maxHealth, 0f, 1f);
        int healthCol = ColorUtil.healthColor(healthPct);

        String hpStr = String.format("HP: %.1f / %.0f", health, maxHealth);
        ctx.drawTextWithShadow(mc.textRenderer, hpStr, x, y + 14, healthCol);

        int barW = panelW - 8;
        int barX = x - 2;
        int barY = y + 25;
        ctx.fill(barX, barY, barX + barW, barY + 4, 0xFF333333);
        ctx.fill(barX, barY, barX + (int)(barW * healthPct), barY + 4, healthCol);

        double dist = mc.player.distanceTo(target);
        String distStr = String.format("Dist: %.1fm", dist);
        ctx.drawTextWithShadow(mc.textRenderer, distStr, x, y + 32, 0xFFAAAAAA);

        String hitsStr = "Hits: " + hitsLanded;
        ctx.drawTextWithShadow(mc.textRenderer, hitsStr, x, y + 42, 0xFFFFAA00);

        if (lastDamageDealt > 0f) {
            String dmgStr = String.format("Last: %.1f dmg", lastDamageDealt);
            long elapsed = System.currentTimeMillis() - lastHitTime;
            int alpha = elapsed < 2000 ? (int)(255 * (1.0 - elapsed / 2000.0)) : 0;
            if (alpha > 0) {
                ctx.drawTextWithShadow(mc.textRenderer, dmgStr, x + 55, y + 42, ColorUtil.withAlpha(0xFF4444, alpha));
            }
        }
    }
}
