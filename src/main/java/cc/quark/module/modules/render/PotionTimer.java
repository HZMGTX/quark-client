package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PotionTimer extends Module {

    private final IntSetting x       = register(new IntSetting("X", "HUD X position", 4, 0, 1920));
    private final IntSetting y       = register(new IntSetting("Y", "HUD Y position", 80, 0, 1080));
    private final BoolSetting color  = register(new BoolSetting("Color", "Color by beneficial/harmful", true));

    public PotionTimer() {
        super("PotionTimer", "Shows active potion effect durations", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        Collection<StatusEffectInstance> effects = mc.player.getStatusEffects();
        if (effects.isEmpty()) return;

        List<StatusEffectInstance> sorted = new ArrayList<>(effects);
        sorted.sort((a, b) -> Integer.compare(b.getDuration(), a.getDuration()));

        int px = x.get();
        int py = y.get();
        int lineH = 11;

        for (int i = 0; i < sorted.size(); i++) {
            StatusEffectInstance eff = sorted.get(i);
            String name = eff.getEffectType().value().getName().getString();
            int amp = eff.getAmplifier() + 1;
            int durTicks = eff.getDuration();
            int durSec   = durTicks / 20;

            String timeStr;
            if (durTicks == 32767) {
                timeStr = "**:**";
            } else {
                timeStr = String.format("%d:%02d", durSec / 60, durSec % 60);
            }

            String label = (amp > 1 ? name + " " + amp : name) + " " + timeStr;

            int textColor;
            if (color.isEnabled()) {
                boolean beneficial = eff.getEffectType().value().isBeneficial();
                // Fade color based on time remaining
                if (!beneficial) {
                    textColor = 0xFFFF5555;
                } else if (durSec < 10) {
                    textColor = 0xFFFFAA00; // Warning: low time
                } else {
                    textColor = 0xFF55FF55;
                }
            } else {
                textColor = 0xFFFFFFFF;
            }

            ctx.drawTextWithShadow(mc.textRenderer, label, px, py + i * lineH, textColor);
        }
    }
}
