package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Collection;

/**
 * PotionEffectHUD - Lists all active potion effects with their amplifiers and durations.
 *
 * Beneficial effects are shown in green, harmful in red, neutral in white.
 * Supports four anchor positions and optional compact mode (shorter names).
 */
public class PotionEffectHUD extends Module {

    private final ModeSetting position = register(new ModeSetting(
            "Position", "HUD anchor position",
            "Top Right", "Top Right", "Top Left", "Bottom Right", "Bottom Left"));
    private final BoolSetting compact     = register(new BoolSetting("Compact",    "Truncate long effect names",       false));
    private final BoolSetting showAmp     = register(new BoolSetting("Amplifier",  "Show Roman numeral amplifier",     true));
    private final BoolSetting showDur     = register(new BoolSetting("Duration",   "Show remaining duration",          true));
    private final BoolSetting background  = register(new BoolSetting("Background", "Draw semi-transparent background", true));
    private final IntSetting  offsetX     = register(new IntSetting ("Offset X",   "Horizontal offset from edge",       4,  0, 200));
    private final IntSetting  offsetY     = register(new IntSetting ("Offset Y",   "Vertical offset from edge",         4,  0, 200));

    public PotionEffectHUD() {
        super("PotionEffectHUD", "Lists all active potion effects with durations on the HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        Collection<StatusEffectInstance> effects = mc.player.getStatusEffects();
        if (effects.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        int sw  = ctx.getScaledWindowWidth();
        int sh  = ctx.getScaledWindowHeight();
        int lh  = mc.textRenderer.fontHeight + 2;
        int ox  = offsetX.get();
        int oy  = offsetY.get();

        // Build lines first so we know the widest one for BG drawing
        String[] lines  = new String[effects.size()];
        int[]    colors = new int[effects.size()];
        int idx = 0;
        for (StatusEffectInstance eff : effects) {
            String name = eff.getEffectType().value().getName().getString();
            if (compact.isEnabled()) name = name.substring(0, Math.min(name.length(), 8));

            int amp = eff.getAmplifier();
            String ampStr = (showAmp.isEnabled() && amp > 0) ? " " + toRoman(amp + 1) : "";

            int durTicks = eff.getDuration();
            String durStr = "";
            if (showDur.isEnabled()) {
                if (durTicks > 9999 * 20) {
                    durStr = " **:**";
                } else {
                    int secs = durTicks / 20;
                    durStr = " " + String.format("%d:%02d", secs / 60, secs % 60);
                }
            }

            lines[idx]  = name + ampStr + durStr;
            boolean beneficial = eff.getEffectType().value().isBeneficial();
            StatusEffectCategory cat = eff.getEffectType().value().getCategory();
            colors[idx] = beneficial ? 0xFF55FF55
                        : cat == StatusEffectCategory.NEUTRAL ? 0xFFFFFFFF
                        : 0xFFFF5555;
            idx++;
        }

        // Determine widest line
        int maxW = 0;
        for (String l : lines) {
            int w = mc.textRenderer.getWidth(l);
            if (w > maxW) maxW = w;
        }

        // Compute anchor
        int x, y;
        switch (position.get()) {
            case "Top Left"      -> { x = ox;               y = oy; }
            case "Bottom Right"  -> { x = sw - maxW - ox;   y = sh - lines.length * lh - oy; }
            case "Bottom Left"   -> { x = ox;               y = sh - lines.length * lh - oy; }
            default              -> { x = sw - maxW - ox;   y = oy; }  // Top Right
        }

        // Background
        if (background.isEnabled()) {
            ctx.fill(x - 2, y - 2, x + maxW + 4, y + lines.length * lh, 0x99111111);
        }

        // Draw lines
        for (int i = 0; i < lines.length; i++) {
            ctx.drawTextWithShadow(mc.textRenderer, lines[i], x, y + i * lh, colors[i]);
        }
    }

    private String toRoman(int n) {
        return switch (n) {
            case 1  -> "I";
            case 2  -> "II";
            case 3  -> "III";
            case 4  -> "IV";
            case 5  -> "V";
            case 6  -> "VI";
            case 7  -> "VII";
            case 8  -> "VIII";
            case 9  -> "IX";
            case 10 -> "X";
            default -> String.valueOf(n);
        };
    }
}
