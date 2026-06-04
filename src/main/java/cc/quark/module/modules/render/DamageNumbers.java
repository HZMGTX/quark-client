package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DamageNumbers extends Module {

    private final ColorSetting color = register(new ColorSetting("Color", "Damage number color", 0xFFFF4444));
    private final BoolSetting crits  = register(new BoolSetting("Crits", "Highlight critical hits differently", true));

    private static class FloatingNumber {
        float   damage;
        boolean crit;
        double  worldX, worldY, worldZ;
        int     life; // ticks remaining

        FloatingNumber(float damage, boolean crit, double x, double y, double z) {
            this.damage = damage;
            this.crit   = crit;
            this.worldX = x + (Math.random() - 0.5) * 0.5;
            this.worldY = y;
            this.worldZ = z + (Math.random() - 0.5) * 0.5;
            this.life   = 30;
        }
    }

    private final List<FloatingNumber> numbers = new ArrayList<>();

    public DamageNumbers() {
        super("DamageNumbers", "Shows floating damage numbers on hit", Category.RENDER);
    }

    @Override
    public void onDisable() {
        numbers.clear();
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null) return;
        // Damage event fires on the local player — show number at player's eye position
        float dmg  = event.getAmount();
        boolean crit = mc.player.fallDistance > 0 && !mc.player.isOnGround()
                    && !mc.player.isClimbing() && !mc.player.isTouchingWater();

        numbers.add(new FloatingNumber(dmg, crit,
                mc.player.getX(), mc.player.getEyeY(), mc.player.getZ()));
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();

        // Decrement lifetimes and remove dead
        Iterator<FloatingNumber> it = numbers.iterator();
        while (it.hasNext()) {
            FloatingNumber fn = it.next();
            fn.life--;
            fn.worldY += 0.04; // Float upward
            if (fn.life <= 0) { it.remove(); continue; }

            // Project to screen
            int[] screen = worldToScreen(fn.worldX, fn.worldY, fn.worldZ);
            if (screen == null) continue;

            float alpha = fn.life / 30f;
            int a       = (int)(alpha * 255);

            String text = String.format("%.1f", fn.damage);
            int c       = color.get();
            int drawColor;

            if (crits.isEnabled() && fn.crit) {
                // Gold crit color
                drawColor = (a << 24) | 0xFFAA00;
                text = text + "!";
            } else {
                int r = (c >> 16) & 0xFF;
                int g = (c >> 8)  & 0xFF;
                int b = c & 0xFF;
                drawColor = (a << 24) | (r << 16) | (g << 8) | b;
            }

            int tw = mc.textRenderer.getWidth(text);
            ctx.drawTextWithShadow(mc.textRenderer, text, screen[0] - tw / 2, screen[1], drawColor);
        }
    }

    private int[] worldToScreen(double wx, double wy, double wz) {
        try {
            net.minecraft.client.render.Camera cam = mc.gameRenderer.getCamera();
            net.minecraft.util.math.Vec3d camPos   = cam.getPos();
            org.joml.Matrix4f proj = mc.gameRenderer.getBasicProjectionMatrix(mc.options.getFov().getValue());
            org.joml.Matrix4f view = new org.joml.Matrix4f();

            float dx = (float)(wx - camPos.x);
            float dy = (float)(wy - camPos.y);
            float dz = (float)(wz - camPos.z);

            org.joml.Vector4f clip = new org.joml.Vector4f(dx, dy, dz, 1.0f);
            view.rotateY((float)Math.toRadians(-cam.getYaw()));
            view.rotateX((float)Math.toRadians(cam.getPitch()));
            clip.mul(view);
            clip.mul(proj);

            if (clip.w <= 0) return null;

            float ndcX = clip.x / clip.w;
            float ndcY = clip.y / clip.w;
            if (Math.abs(ndcX) > 1.1f || Math.abs(ndcY) > 1.1f) return null;

            int sw = mc.getWindow().getScaledWidth();
            int sh = mc.getWindow().getScaledHeight();
            int sx = (int)((ndcX + 1f) / 2f * sw);
            int sy = (int)((1f - ndcY) / 2f * sh);
            return new int[]{sx, sy};
        } catch (Exception e) {
            return null;
        }
    }
}
