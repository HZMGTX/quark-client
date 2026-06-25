package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class EffectDisplay extends Module {
    private final IntSetting x = register(new IntSetting("X", "HUD X", 200, 0, 1920));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y", 10, 0, 1080));
    private final BoolSetting showDuration = register(new BoolSetting("Duration", "Show time remaining", true));

    public EffectDisplay() {
        super("Effect Display", "Shows active potion effects on HUD", Category.RENDER, 0);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int yOff = 0;
        for (var effect : mc.player.getStatusEffects()) {
            String name = effect.getEffectType().value().getName().getString();
            int amp = effect.getAmplifier() + 1;
            String durStr = showDuration.isEnabled() ? " " + (effect.getDuration() / 20) + "s" : "";
            ctx.drawText(mc.textRenderer, "§b" + name + " " + amp + durStr, x.get(), y.get() + yOff, 0xFFFFFF, true);
            yOff += 12;
        }
    }
}
