package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

public class StatusEffectTimer extends Module {
    private final BoolSetting showAmplifier = register(new BoolSetting("ShowAmplifier", "Show effect level", true));
    private final BoolSetting countdown = register(new BoolSetting("Countdown", "Show remaining time", true));
    public StatusEffectTimer() { super("StatusEffectTimer", "Shows status effect timers on screen", Category.RENDER); }
    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int y = 30, x = 5;
        for (StatusEffectInstance e : mc.player.getStatusEffects()) {
            String name = e.getEffectType().getName().getString();
            String txt = name + (showAmplifier.getValue() ? " " + (e.getAmplifier() + 1) : "");
            if (countdown.getValue()) txt += " " + (e.getDuration() / 20) + "s";
            ctx.drawText(mc.textRenderer, txt, x, y, 0xFFFFFFFF, true);
            y += 12;
        }
    }
}
