package cc.quark.module.modules.render;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class TargetHUD extends Module {

    public final IntSetting x = register(new IntSetting("X", "X position", 400, 0, 3000));
    public final IntSetting y = register(new IntSetting("Y", "Y position", 300, 0, 3000));

    private LivingEntity target;
    private long lastAttackTime;
    private float animatedHealth = 0;

    public TargetHUD() {
        super("TargetHUD", "Displays information about your combat target", Category.RENDER);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (event.getTarget() instanceof LivingEntity le) {
            target = le;
            lastAttackTime = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        // Auto-clear target after 3 seconds of no attacks
        if (target != null && (System.currentTimeMillis() - lastAttackTime > 3000 || target.isDead() || !target.isAlive())) {
            target = null;
        }

        // Check if KillAura has a target to override
        Module ka = Quark.getInstance().getModuleManager().getModule("KillAura");
        if (ka != null && ka.isEnabled()) {
            if (ka instanceof cc.quark.module.modules.combat.KillAura aura) {
                if (aura.getTarget() instanceof LivingEntity le) {
                    target = le;
                    lastAttackTime = System.currentTimeMillis();
                }
            }
        }

        if (target == null) return;

        DrawContext ctx = event.getDrawContext();
        int posX = x.get();
        int posY = y.get();
        int width = 140;
        int height = 45;

        // Sleek Background
        ctx.fill(posX, posY, posX + width, posY + height, 0xBB181818);
        ctx.fill(posX, posY, posX + width, posY + 1, cc.quark.gui.ClickGUI.getAccentColor()); // Top border

        // Name
        cc.quark.util.RenderUtil.drawCustomText(ctx, target.getName().getString(), posX + 40, posY + 6, 0xFFFFFFFF);

        // Health text
        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        float healthPct = MathHelper.clamp(health / maxHealth, 0.0f, 1.0f);
        
        // Smooth health animation
        if (animatedHealth == 0) animatedHealth = healthPct;
        animatedHealth += (healthPct - animatedHealth) * 0.1f * event.getTickDelta();
        
        String hpStr = String.format("%.1f \u2764", health / 2.0f); // Hearts
        cc.quark.util.RenderUtil.drawCustomText(ctx, hpStr, posX + 40, posY + 18, getHealthColor(healthPct));

        // Health bar background
        int barX = posX + 40;
        int barY = posY + 32;
        int barWidth = 90;
        int barHeight = 6;
        ctx.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);

        // Animated Health bar fill
        int fillWidth = (int)(barWidth * animatedHealth);
        ctx.fill(barX, barY, barX + fillWidth, barY + barHeight, getHealthColor(animatedHealth));

        // Draw Player Head (if target is player)
        if (target instanceof AbstractClientPlayerEntity playerTarget) {
            ctx.drawTexture(playerTarget.getSkinTextures().texture(), posX + 5, posY + 8, 30, 30, 8, 8, 8, 8, 64, 64);
        } else {
            // Draw a placeholder generic face or rect for mobs
            ctx.fill(posX + 5, posY + 8, posX + 35, posY + 38, 0xFF555555);
        }
    }

    private int getHealthColor(float pct) {
        if (pct > 0.6f) return 0xFF00FF44;
        if (pct > 0.3f) return 0xFFFFFF00;
        return 0xFFFF2222;
    }
}
