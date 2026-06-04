package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.entity.boss.BossBar;

import java.util.Map;
import java.util.UUID;

/**
 * BossbarMod - Can hide the vanilla boss bar entirely, or re-render it with a custom color.
 * Rendered in EventRender2D to overlay custom drawing; the vanilla bar is suppressed via mixin.
 */
public class BossbarMod extends Module {

    private final BoolSetting hide = register(new BoolSetting(
            "Hide", "Completely hide the boss bar", false));
    private final ColorSetting color = register(new ColorSetting(
            "Color", "Custom boss bar color", 0xFFFF55FF));

    public BossbarMod() {
        super("BossbarMod", "Customizes boss bar appearance", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.inGameHud == null) return;
        if (hide.isEnabled()) return; // Vanilla bar suppressed via InGameHud mixin

        DrawContext ctx = event.getDrawContext();
        BossBarHud bossBarHud = mc.inGameHud.getBossBarHud();
        if (bossBarHud == null) return;

        // Access boss bars via the map field (reflection)
        try {
            var field = BossBarHud.class.getDeclaredField("bossBars");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, ?> bars = (Map<UUID, ?>) field.get(bossBarHud);
            if (bars.isEmpty()) return;

            int sw = mc.getWindow().getScaledWidth();
            int barW = 182;
            int startX = sw / 2 - barW / 2;
            int y = 12;
            int c = color.get();

            for (var entry : bars.entrySet()) {
                // Draw custom bar
                var clientBossBar = entry.getValue();
                float percent = 0f;
                String name = "Boss";
                try {
                    var percentField = clientBossBar.getClass().getDeclaredMethod("getPercent");
                    percentField.setAccessible(true);
                    percent = (float) percentField.invoke(clientBossBar);
                    var nameMethod = clientBossBar.getClass().getDeclaredMethod("getName");
                    nameMethod.setAccessible(true);
                    name = nameMethod.invoke(clientBossBar).toString();
                } catch (Exception ignored) {}

                // Background
                ctx.fill(startX, y, startX + barW, y + 5, 0xFF333333);
                // Fill
                ctx.fill(startX, y, startX + (int)(barW * percent), y + 5, c);
                // Border
                ctx.fill(startX, y, startX + barW, y + 1, 0xFF888888);
                ctx.fill(startX, y + 4, startX + barW, y + 5, 0xFF888888);
                // Label
                ctx.drawTextWithShadow(mc.textRenderer, name,
                        sw / 2 - mc.textRenderer.getWidth(name) / 2, y - 9, c);
                y += 18;
            }
        } catch (Exception ignored) {}
    }

    /** Called by a mixin to check whether to suppress vanilla rendering. */
    public boolean shouldHide() {
        return isEnabled() && hide.isEnabled();
    }
}
