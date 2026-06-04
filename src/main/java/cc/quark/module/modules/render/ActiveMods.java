package cc.quark.module.modules.render;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.*;
import net.minecraft.client.gui.DrawContext;

import java.util.*;

public class ActiveMods extends Module {

    private final BoolSetting showLogo    = register(new BoolSetting("Show Logo",    "Show client name above module list", true));
    private final BoolSetting showAccent  = register(new BoolSetting("Show Accent",  "Show colored bar next to modules",   true));
    private final BoolSetting rightAlign  = register(new BoolSetting("Right Align",  "Align list to right side of screen", true));
    private final BoolSetting lowercase   = register(new BoolSetting("Lowercase",    "Display module names in lowercase",  false));
    private final DoubleSetting speed     = register(new DoubleSetting("Speed",   "Slide-in animation speed",     2.5, 0.5, 10.0));
    private final DoubleSetting gap       = register(new DoubleSetting("Gap",     "Vertical gap between entries", 0.35, 0.0, 5.0));
    private final DoubleSetting padding   = register(new DoubleSetting("Padding", "Horizontal text padding",      3.0,  0.0, 12.0));
    private final DoubleSetting opacity   = register(new DoubleSetting("Opacity", "Row background opacity (0=none)", 0.0, 0.0, 1.0));
    private final ColorSetting  theme     = register(new ColorSetting("Theme",     "Accent bar and text color",  0xFF00AAFF));
    private final ColorSetting  logoTint  = register(new ColorSetting("Logo Tint", "Logo text color",            0xFFFFFFFF));

    private final Map<String, Float> slideProgress = new HashMap<>();

    public ActiveMods() {
        super("ActiveMods", "RAIN-style HUD: logo + right-aligned module list with accent bar", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.getWindow() == null || mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw   = mc.getWindow().getScaledWidth();
        int fontH = mc.textRenderer.fontHeight;

        boolean  right     = rightAlign.isEnabled();
        int      pad       = Math.max(1, (int) padding.get());
        int      gapPx     = Math.max(1, (int)(fontH * gap.get() * 0.35f) + 1);
        int      rowH      = fontH + gapPx;
        int      barW      = showAccent.isEnabled() ? 3 : 0;
        float    animSpeed = (float)(speed.get() * 0.07f);
        int      accentClr = theme.get();

        // Collect enabled, visible modules sorted by text width (widest first = stable layout)
        List<Module> enabled = Quark.getInstance().getModuleManager().getModules().stream()
                .filter(m -> m.isEnabled() && m.isVisible() && m != this)
                .sorted(Comparator.comparingInt(
                        (Module m) -> mc.textRenderer.getWidth(fmt(m.getName()))).reversed())
                .toList();

        // Tick slide animations
        for (String key : new ArrayList<>(slideProgress.keySet())) {
            if (enabled.stream().noneMatch(m -> m.getName().equals(key))) {
                float p = slideProgress.get(key) - animSpeed;
                if (p <= 0f) slideProgress.remove(key); else slideProgress.put(key, p);
            }
        }
        for (Module m : enabled) {
            slideProgress.merge(m.getName(), animSpeed, (old, v) -> Math.min(1f, old + v));
        }

        // ── Logo ────────────────────────────────────────────────────────────
        int startY = 4;
        if (showLogo.isEnabled()) {
            final float logoScale = 1.8f;
            String logoText = "QUARK";
            int logoW = (int)(mc.textRenderer.getWidth(logoText) * logoScale);
            int logoH = (int)(fontH * logoScale);

            int logoX;
            if (right) {
                logoX = sw - logoW - barW - pad - 2;
            } else {
                logoX = barW + pad + 2;
            }

            ctx.getMatrices().push();
            ctx.getMatrices().translate(logoX, startY, 0);
            ctx.getMatrices().scale(logoScale, logoScale, 1f);
            // Shadow pass
            ctx.drawText(mc.textRenderer, logoText, 1, 1, 0x55000000, false);
            // Main pass
            ctx.drawText(mc.textRenderer, logoText, 0, 0, logoTint.get(), false);
            ctx.getMatrices().pop();

            startY += logoH + 5;
        }

        // ── Module rows ─────────────────────────────────────────────────────
        int maxTextW = enabled.stream()
                .mapToInt(m -> mc.textRenderer.getWidth(fmt(m.getName())))
                .max().orElse(0);

        // total row width = textW + padding both sides + bar
        int totalW = maxTextW + pad * 2 + barW;

        int y = startY;

        for (Module mod : enabled) {
            String name    = fmt(mod.getName());
            int    textW   = mc.textRenderer.getWidth(name);
            float  slide   = slideProgress.getOrDefault(mod.getName(), 1f);
            int    slideOff = (int)((1f - slide) * (totalW + 6));

            int textX, barX, bgX1, bgX2;
            if (right) {
                barX  = sw - barW - slideOff;
                textX = barX  - pad - textW;
                bgX1  = textX - pad;
                bgX2  = barX  + barW;
            } else {
                barX  = slideOff;
                textX = barX + barW + pad;
                bgX1  = barX;
                bgX2  = textX + textW + pad;
            }

            // Background
            int bgAlpha = (int)(opacity.get() * 200);
            if (bgAlpha > 4) {
                ctx.fill(bgX1, y - 1, bgX2, y + fontH + 1,
                        (bgAlpha << 24) | 0x000000);
            }

            // Accent bar
            if (showAccent.isEnabled()) {
                ctx.fill(barX, y - 1, barX + barW, y + fontH + 1, accentClr);
            }

            // Module name
            ctx.drawTextWithShadow(mc.textRenderer, name, textX, y, accentClr);

            // Suffix (e.g. speed value)
            String suffix = mod.getSuffix();
            if (suffix != null && !suffix.isEmpty()) {
                ctx.drawTextWithShadow(mc.textRenderer, " " + suffix,
                        textX + textW, y, 0xFF888888);
            }

            y += rowH;
        }
    }

    private String fmt(String name) {
        return lowercase.isEnabled() ? name.toLowerCase(java.util.Locale.ROOT) : name;
    }
}
