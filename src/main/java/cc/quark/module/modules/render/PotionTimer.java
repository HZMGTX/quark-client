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
import java.util.List;

public class PotionTimer extends Module {

    private final BoolSetting compact = register(new BoolSetting("Compact", "Show only the time, no effect name", false));
    private final IntSetting x = register(new IntSetting("X", "HUD X position", 4, 0, 3840));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 80, 0, 2160));

    public PotionTimer() {
        super("PotionTimer", "Shows duration remaining for each active potion effect", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        List<StatusEffectInstance> effects = new ArrayList<>(mc.player.getStatusEffects());
        if (effects.isEmpty()) return;

        effects.sort((a, b) -> Integer.compare(b.getDuration(), a.getDuration()));

        DrawContext ctx = event.getDrawContext();
        int px = x.get(), py = y.get();
        int lineH = 11;

        for (int i = 0; i < effects.size(); i++) {
            StatusEffectInstance eff = effects.get(i);

            int durTicks = eff.getDuration();
            String timeStr;
            if (durTicks == 32767) {
                timeStr = "**:**";
            } else {
                int durSec = durTicks / 20;
                timeStr = String.format("%d:%02d", durSec / 60, durSec % 60);
            }

            String label;
            if (compact.isEnabled()) {
                label = timeStr;
            } else {
                String name = eff.getEffectType().value().getName().getString();
                int amp = eff.getAmplifier() + 1;
                label = (amp > 1 ? name + " " + amp : name) + " " + timeStr;
            }

            boolean beneficial = eff.getEffectType().value().isBeneficial();
            int durSec = durTicks / 20;
            int textColor;
            if (!beneficial) {
                textColor = 0xFFFF5555;
            } else if (durSec < 10) {
                textColor = 0xFFFFAA00;
            } else {
                textColor = 0xFF55FF55;
            }

            ctx.drawTextWithShadow(mc.textRenderer, label, px, py + i * lineH, textColor);
        }
    }
}
