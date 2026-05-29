package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ColorUtil;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * DamageTag — records damage events per entity and renders floating damage
 * numbers above their heads that fade out over 2 seconds.
 * Also displays a static health tag every tick.
 */
public class DamageTag extends Module {

    private final BoolSetting   healthTag  = register(new BoolSetting  ("Health Tag",   "Show current HP above entity",     true));
    private final BoolSetting   onlyPlayers= register(new BoolSetting  ("Only Players", "Only tag players",                 false));
    private final DoubleSetting range      = register(new DoubleSetting("Range",        "Max distance to show tags",        30.0, 2.0, 128.0));
    private final BoolSetting   background = register(new BoolSetting  ("Background",   "Draw a translucent background",    true));

    /** entity id -> [last known HP] to compute damage deltas */
    private final Map<Integer, Float> lastHealth = new HashMap<>();
    /** entity id -> [damage amount, timestamp ms] */
    private final Map<Integer, float[]> damageEvents = new HashMap<>();

    private static final long FADE_MS = 2000L;

    public DamageTag() {
        super("DamageTag", "Renders floating damage numbers and health tags above entities", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastHealth.clear();
        damageEvents.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead()) continue;

            float hp = living.getHealth();
            float prev = lastHealth.getOrDefault(entity.getId(), hp);
            float dmg = prev - hp;
            if (dmg > 0.01f) {
                damageEvents.put(entity.getId(), new float[]{ dmg, now });
            }
            lastHealth.put(entity.getId(), hp);
        }

        // Evict old entries
        Iterator<Map.Entry<Integer, float[]>> it = damageEvents.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, float[]> entry = it.next();
            if (now - (long) entry.getValue()[1] > FADE_MS) it.remove();
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null || mc.textRenderer == null) return;
        DrawContext ctx = event.getDrawContext();
        long now = System.currentTimeMillis();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead()) continue;
            if (onlyPlayers.isEnabled() && !(entity instanceof PlayerEntity)) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;

            float baseY = entity.getY() + entity.getHeight() + 0.35f;

            // Health tag
            if (healthTag.isEnabled()) {
                Vec3d hPos = new Vec3d(entity.getX(), baseY, entity.getZ());
                double[] hScreen = RenderUtil.project(hPos);
                if (hScreen != null) {
                    float pct = living.getMaxHealth() > 0 ? living.getHealth() / living.getMaxHealth() : 1f;
                    int color = ColorUtil.healthColor(pct) | 0xFF000000;
                    String tag = String.format("%.1f", living.getHealth());
                    int w = mc.textRenderer.getWidth(tag);
                    int sx = (int) hScreen[0] - w / 2;
                    int sy = (int) hScreen[1];
                    if (background.isEnabled()) ctx.fill(sx - 2, sy - 1, sx + w + 2, sy + 9, 0x88000000);
                    ctx.drawText(mc.textRenderer, tag, sx, sy, color, true);
                    baseY += 0.25;
                }
            }

            // Damage float
            float[] dmgData = damageEvents.get(entity.getId());
            if (dmgData != null) {
                long elapsed = now - (long) dmgData[1];
                float alpha = Math.max(0f, 1f - (float) elapsed / FADE_MS);
                int a = (int) (alpha * 255);
                if (a <= 0) continue;

                // Float upward as it fades
                double floatOffset = elapsed / 1000.0 * 0.5;
                Vec3d dPos = new Vec3d(entity.getX(), baseY + floatOffset, entity.getZ());
                double[] dScreen = RenderUtil.project(dPos);
                if (dScreen == null) continue;

                String dmgText = "-" + String.format("%.1f", dmgData[0]);
                int w = mc.textRenderer.getWidth(dmgText);
                int sx = (int) dScreen[0] - w / 2;
                int sy = (int) dScreen[1];
                int color = (a << 24) | 0xFF5555;
                ctx.drawText(mc.textRenderer, dmgText, sx, sy, color, true);
            }
        }
    }
}
