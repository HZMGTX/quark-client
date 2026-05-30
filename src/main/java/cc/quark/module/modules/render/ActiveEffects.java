package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Collection;

public class ActiveEffects extends Module {

    private final BoolSetting showAmplifier = register(new BoolSetting("ShowAmplifier", "Show effect amplifier level", true));
    private final IntSetting posX = register(new IntSetting("X", "HUD X position", 4, 0, 500));
    private final IntSetting posY = register(new IntSetting("Y", "HUD Y position", 100, 0, 500));

    public ActiveEffects() {
        super("ActiveEffects", "Renders active potion effects with icons, amplifier, and remaining duration", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        Collection<StatusEffectInstance> effects = mc.player.getStatusEffects();
        if (effects.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        int x = posX.get();
        int y = posY.get();
        int lineH = mc.textRenderer.fontHeight + 2;

        // Header
        ctx.drawTextWithShadow(mc.textRenderer, "Effects:", x, y, 0xFFFFFFFF);
        y += lineH + 1;

        for (StatusEffectInstance effect : effects) {
            String name = effect.getEffectType().value().getName().getString();
            int amp = effect.getAmplifier() + 1;
            int durTicks = effect.getDuration();
            boolean beneficial = effect.getEffectType().value().isBeneficial();
            int nameColor = beneficial ? 0xFF55FF55 : 0xFFFF5555;

            StringBuilder line = new StringBuilder();
            line.append(name);
            if (showAmplifier.isEnabled() && amp > 1) {
                line.append(" ").append(amp);
            }

            ctx.drawTextWithShadow(mc.textRenderer, line.toString(), x, y, nameColor);

            // Duration on the right side
            String durStr;
            if (durTicks == 32767) {
                durStr = "**:**";
            } else {
                int totalSec = durTicks / 20;
                durStr = String.format("%d:%02d", totalSec / 60, totalSec % 60);
            }
            int textWidth = mc.textRenderer.getWidth(line.toString());
            ctx.drawTextWithShadow(mc.textRenderer, " " + durStr, x + textWidth, y, 0xFFAAAAAA);
            y += lineH;
        }
    }
}
