package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * ComboCounter - Tracks consecutive hits on entities and displays a combo counter HUD.
 * Resets if no new damage is dealt within comboTimeout milliseconds.
 */
public class ComboCounter extends Module {

    private final IntSetting comboTimeout = register(new IntSetting(
            "Timeout", "Milliseconds before combo resets", 2000, 200, 10000));
    private final ColorSetting color = register(new ColorSetting(
            "Color", "Combo text color", 0xFFFF5500));

    private int combo      = 0;
    private int maxCombo   = 0;
    private long lastHitMs = 0;

    private final Map<Integer, Float> prevHealth = new HashMap<>();

    public ComboCounter() {
        super("ComboCounter", "Shows combo hit counter during fights", Category.RENDER);
    }

    @Override
    public void onEnable() {
        combo     = 0;
        maxCombo  = 0;
        lastHitMs = 0;
        prevHealth.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // Reset combo on timeout
        if (combo > 0 && System.currentTimeMillis() - lastHitMs > comboTimeout.get()) {
            combo = 0;
        }

        // Detect hits
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == mc.player) continue;
            float prev = prevHealth.getOrDefault(entity.getId(), living.getHealth());
            float cur  = living.getHealth();
            if (cur < prev) {
                combo++;
                if (combo > maxCombo) maxCombo = combo;
                lastHitMs = System.currentTimeMillis();
            }
            prevHealth.put(entity.getId(), cur);
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (combo <= 1) return;
        DrawContext ctx = event.getDrawContext();

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        String comboStr  = combo + "x COMBO";
        String maxStr    = "Best: " + maxCombo;

        float scale = 1.0f + Math.min(1.0f, (combo - 2) * 0.05f);
        int c = color.get();

        int cx = sw / 2 - (int)(mc.textRenderer.getWidth(comboStr) * scale / 2);
        int cy = sh / 3;

        ctx.drawTextWithShadow(mc.textRenderer, comboStr, cx, cy, c);
        ctx.drawTextWithShadow(mc.textRenderer, maxStr,
                sw / 2 - mc.textRenderer.getWidth(maxStr) / 2, cy + 11, 0xFFAAAAAA);
    }
}
