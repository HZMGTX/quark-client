package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ambiance extends Module {

    private final ModeSetting   effect      = register(new ModeSetting("Effect", "Visual effect type", "Vignette", "Vignette", "Particles", "Glow", "None"));
    private final BoolSetting   pulse       = register(new BoolSetting("Pulse", "Breathing/pulse animation", true));
    private final DoubleSetting gamma       = register(new DoubleSetting("Gamma", "Game gamma override (1.0=default)", 1.0, 0.0, 15.0));
    private final BoolSetting   applyGamma  = register(new BoolSetting("Apply Gamma", "Override the game gamma setting", false));
    private final DoubleSetting intensity   = register(new DoubleSetting("Intensity", "Effect intensity", 0.5, 0.0, 1.0));

    private double savedGamma = 1.0;

    private static class Particle {
        float x, y, vx, vy, life, maxLife, size;
    }

    private final List<Particle> particles = new ArrayList<>();
    private final Random rng = new Random();

    public Ambiance() {
        super("Ambiance", "Ambient visual effects: vignette, particles, glow, with optional pulse animation", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) savedGamma = mc.options.getGamma().getValue();
    }

    @Override
    public void onDisable() {
        if (mc.options != null && applyGamma.isEnabled()) {
            mc.options.getGamma().setValue(savedGamma);
        }
        particles.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.options == null) return;
        if (applyGamma.isEnabled()) mc.options.getGamma().setValue(gamma.get());
        if (mc.getWindow() == null) return;

        if (effect.is("Particles")) {
            int sw = mc.getWindow().getScaledWidth();
            int sh = mc.getWindow().getScaledHeight();
            if (particles.size() < 30 && rng.nextFloat() < 0.3f) {
                Particle p = new Particle();
                p.x = rng.nextFloat() * sw;
                p.y = sh + 5;
                p.vx = (rng.nextFloat() - 0.5f) * 0.5f;
                p.vy = -(0.3f + rng.nextFloat() * 0.7f);
                p.maxLife = 80 + rng.nextInt(80);
                p.life = p.maxLife;
                p.size = 1 + rng.nextFloat() * 2;
                particles.add(p);
            }
            particles.forEach(p -> {
                p.x += p.vx;
                p.y += p.vy;
                p.life--;
            });
            particles.removeIf(p -> p.life <= 0 || p.y < -10);
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.getWindow() == null) return;
        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        float pulseAlpha = 1.0f;
        if (pulse.isEnabled()) {
            double t = (System.currentTimeMillis() % 3000L) / 3000.0;
            pulseAlpha = (float)(0.7 + 0.3 * Math.sin(t * Math.PI * 2));
        }

        float base = (float) intensity.get() * pulseAlpha;

        switch (effect.get()) {
            case "Vignette" -> drawVignette(ctx, sw, sh, base);
            case "Glow"     -> drawGlow(ctx, sw, sh, base);
            case "Particles" -> {
                drawParticles(ctx);
                drawVignette(ctx, sw, sh, base * 0.4f);
            }
        }
    }

    private void drawVignette(DrawContext ctx, int sw, int sh, float alpha) {
        int a = (int)(alpha * 180);
        int steps = 40;
        for (int i = 0; i < steps; i++) {
            float t = (float) i / steps;
            int ea = (int)(a * t * t);
            int col = (ea << 24);
            ctx.fill(i, i, sw - i, i + 1, col);
            ctx.fill(i, sh - i - 1, sw - i, sh - i, col);
            ctx.fill(i, i, i + 1, sh - i, col);
            ctx.fill(sw - i - 1, i, sw - i, sh - i, col);
        }
    }

    private void drawGlow(DrawContext ctx, int sw, int sh, float alpha) {
        int accent = cc.quark.gui.ClickGUI.getAccentColor();
        int r = (accent >> 16) & 0xFF;
        int g = (accent >> 8) & 0xFF;
        int b = accent & 0xFF;
        int steps = 20;
        for (int i = 0; i < steps; i++) {
            float t = (float) i / steps;
            int a = (int)(alpha * 60 * (1 - t));
            int col = (a << 24) | (r << 16) | (g << 8) | b;
            ctx.fill(i, i, sw - i, i + 1, col);
            ctx.fill(i, sh - i - 1, sw - i, sh - i, col);
            ctx.fill(i, i, i + 1, sh - i, col);
            ctx.fill(sw - i - 1, i, sw - i, sh - i, col);
        }
    }

    private void drawParticles(DrawContext ctx) {
        int accent = cc.quark.gui.ClickGUI.getAccentColor();
        int r = (accent >> 16) & 0xFF;
        int g = (accent >> 8) & 0xFF;
        int b = accent & 0xFF;
        for (Particle p : particles) {
            float lifePct = p.life / p.maxLife;
            int a = (int)(lifePct * 180);
            int col = (a << 24) | (r << 16) | (g << 8) | b;
            int px = (int) p.x;
            int py = (int) p.y;
            int s = (int) p.size;
            ctx.fill(px, py, px + s, py + s, col);
        }
    }
}
