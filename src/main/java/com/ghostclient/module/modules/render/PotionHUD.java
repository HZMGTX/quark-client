package com.ghostclient.module.modules.render;

import com.ghostclient.GhostClient;
import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventRender2D;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.ModeSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Formatting;

import java.util.Collection;

public class PotionHUD extends Module {

    private final ModeSetting position = register(new ModeSetting("Position", "HUD position", "Top Right", "Top Right", "Top Left", "Bottom Right", "Bottom Left"));
    private final BoolSetting compact = register(new BoolSetting("Compact", "Compact display", false));

    public PotionHUD() {
        super("PotionHUD", "Shows active potion effects", Category.RENDER, 0);
    }

    @Override
    public void onEnable() {
        GhostClient.getInstance().getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        GhostClient.getInstance().getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getContext();
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        Collection<StatusEffectInstance> effects = mc.player.getStatusEffects();
        if (effects.isEmpty()) return;

        int x = sw - 120, y = 10;
        switch (position.getValue()) {
            case "Top Left" -> { x = 10; y = 10; }
            case "Bottom Right" -> { x = sw - 120; y = sh - effects.size() * 12 - 10; }
            case "Bottom Left" -> { x = 10; y = sh - effects.size() * 12 - 10; }
        }

        int i = 0;
        for (StatusEffectInstance effect : effects) {
            String name = effect.getEffectType().getName().getString();
            int amp = effect.getAmplifier() + 1;
            int dur = effect.getDuration() / 20;
            String durStr = dur > 600 ? "**:**" : String.format("%d:%02d", dur / 60, dur % 60);
            boolean bad = effect.getEffectType().isBeneficial() == false;
            int color = bad ? 0xFFFF5555 : 0xFF55FF55;
            String text = compact.getValue()
                ? name.substring(0, Math.min(name.length(), 8)) + " " + amp
                : name + " " + (amp > 1 ? amp + " " : "") + durStr;
            ctx.drawTextWithShadow(mc.textRenderer, text, x, y + i * 12, color);
            i++;
        }
    }
}
