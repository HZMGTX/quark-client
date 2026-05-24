package cc.quark.module.modules.render;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.List;

/**
 * HUD - main heads-up display overlay.
 *
 * Renders:
 *  - Client watermark (top-left)
 *  - Enabled modules list (right side, sorted by name length descending)
 *  - Armor durability (bottom-center)
 *  - Coordinates / FPS / Ping (bottom-left)
 *  - Speed / BPS
 *  - Active potion effects
 */
public class HUD extends Module {

    public enum ColorScheme {
        RAINBOW, STATIC, GRADIENT
    }

    private final ModeSetting colorScheme = register(new ModeSetting(
            "Color", "Color scheme for the HUD text", "Rainbow", "Rainbow", "Static", "Gradient"));

    private final BoolSetting watermark = register(new BoolSetting(
            "Watermark", "Show client name and version", true));

    private final BoolSetting moduleList = register(new BoolSetting(
            "Module List", "Show enabled modules on right side", true));

    private final BoolSetting coordinates = register(new BoolSetting(
            "Coordinates", "Show player coordinates", true));

    private final BoolSetting potionEffects = register(new BoolSetting(
            "Potion Effects", "Show active potion effects", true));

    // Previous position for BPS calculation
    private double prevX, prevZ;
    private double bps;

    public HUD() {
        super("HUD", "Main HUD overlay with module list and player info", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        DrawContext ctx = event.getDrawContext();
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();
        ClientPlayerEntity player = mc.player;

        // BPS calculation
        double dx = player.getX() - prevX;
        double dz = player.getZ() - prevZ;
        bps = Math.sqrt(dx * dx + dz * dz) * 20.0; // per tick * 20 = per second
        prevX = player.getX();
        prevZ = player.getZ();

        // ---- Watermark ----
        if (watermark.isEnabled()) {
            String wmText = Quark.MOD_NAME + " Â§7v" + Quark.VERSION;
            int wmColor = getAccentColor(0);
            ctx.drawTextWithShadow(mc.textRenderer, wmText, 3, 3, wmColor);
        }

        // ---- Module List (right side) ----
        if (moduleList.isEnabled()) {
            List<Module> enabled = Quark.getInstance().getModuleManager().getEnabledModules();
            // getEnabledModules() already sorts by name length ascending; reverse for descending
            int yOffset = 3;
            for (int i = enabled.size() - 1; i >= 0; i--) {
                Module mod = enabled.get(i);
                if (!mod.isVisible()) continue;
                if (mod == this) continue; // Don't list HUD itself
                String modName = mod.getName();
                int textWidth = mc.textRenderer.getWidth(modName);
                int x = screenW - textWidth - 3;
                int color = getAccentColor((float)(enabled.size() - 1 - i) / Math.max(1, enabled.size() - 1));

                // Draw a dark background bar
                ctx.fill(screenW - textWidth - 5, yOffset - 1,
                        screenW, yOffset + 9, 0x55000000);
                // Colored left accent bar
                ctx.fill(screenW - textWidth - 5, yOffset - 1,
                        screenW - textWidth - 3, yOffset + 9, color | 0xFF000000);
                ctx.drawTextWithShadow(mc.textRenderer, modName, x, yOffset, 0xFFFFFFFF);
                yOffset += 11;
            }
        }

        // ---- Coordinates / FPS / BPS (bottom-left) ----
        if (coordinates.isEnabled()) {
            int y = screenH - 30;
            String coordStr = String.format("XYZ: %.1f / %.1f / %.1f",
                    player.getX(), player.getY(), player.getZ());
            ctx.drawTextWithShadow(mc.textRenderer, coordStr, 3, y, 0xFFFFFFFF);

            String fpsStr = "FPS: " + mc.getCurrentFps();
            ctx.drawTextWithShadow(mc.textRenderer, fpsStr, 3, y + 10, 0xFFAAAAAA);

            String bpsStr = String.format("BPS: %.2f", bps);
            ctx.drawTextWithShadow(mc.textRenderer, bpsStr, 3, y + 20, 0xFFAAAAAA);
        }

        // ---- Ping ----
        {
            var networkHandler = mc.getNetworkHandler();
            if (networkHandler != null) {
                var entry = networkHandler.getPlayerListEntry(player.getUuid());
                if (entry != null) {
                    int ping = entry.getLatency();
                    String pingStr = "Ping: " + ping + "ms";
                    ctx.drawTextWithShadow(mc.textRenderer, pingStr, 3, screenH - 40, 0xFFAAAAAA);
                }
            }
        }

        // ---- Armor Durability (bottom-center) ----
        renderArmorDurability(ctx, screenW, screenH, player);

        // ---- Potion Effects ----
        if (potionEffects.isEnabled()) {
            renderPotionEffects(ctx, screenW, screenH, player);
        }
    }

    private void renderArmorDurability(DrawContext ctx, int screenW, int screenH, ClientPlayerEntity player) {
        int[] armorSlots = {36, 37, 38, 39};
        String[] names = {"Boots", "Legs", "Chest", "Helm"};
        int startX = screenW / 2 - 80;
        int y = screenH - 22;

        for (int i = 0; i < 4; i++) {
            ItemStack armor = player.getInventory().getStack(armorSlots[i]);
            if (armor.isEmpty() || !armor.isDamageable()) continue;

            int maxDmg = armor.getMaxDamage();
            int dmg = armor.getDamage();
            float pct = (float)(maxDmg - dmg) / maxDmg;
            int barW = 30;
            int filledW = (int)(barW * pct);

            int barColor = pct > 0.6f ? 0xFF00FF44 : pct > 0.3f ? 0xFFFFFF00 : 0xFFFF2222;

            int x = startX + i * 40;
            // Background
            ctx.fill(x, y, x + barW, y + 4, 0x88000000);
            // Fill
            ctx.fill(x, y, x + filledW, y + 4, barColor);
            // Label
            ctx.drawTextWithShadow(mc.textRenderer, names[i].substring(0, 1),
                    x + 13, y + 6, 0xFFCCCCCC);
        }
    }

    private void renderPotionEffects(DrawContext ctx, int screenW, int screenH, ClientPlayerEntity player) {
        int x = 3;
        int y = screenH / 2 - 40;

        for (StatusEffectInstance effect : player.getStatusEffects()) {
            String name = effect.getEffectType().getName().getString();
            int amp = effect.getAmplifier() + 1;
            int dur = effect.getDuration() / 20; // convert ticks to seconds
            String line = name + (amp > 1 ? " " + (amp) : "")
                    + " Â§7(" + formatDuration(dur) + ")Â§r";
            ctx.drawTextWithShadow(mc.textRenderer, line, x, y, 0xFFFFFFFF);
            y += 11;
        }
    }

    private String formatDuration(int seconds) {
        if (seconds > 9999) return "**:**";
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    /**
     * Returns a color based on the current color scheme.
     *
     * @param t normalized position 0..1 along the module list
     */
    private int getAccentColor(float t) {
        return switch (colorScheme.get()) {
            case "Rainbow" -> {
                float hue = (System.currentTimeMillis() % 2000L) / 2000.0f + t * 0.15f;
                yield Color.HSBtoRGB(hue % 1.0f, 1.0f, 1.0f);
            }
            case "Gradient" -> {
                // Blue to purple gradient
                int r = (int)(0x80 + t * 0x7F);
                int g = 0x00;
                int b = (int)(0xFF - t * 0x7F);
                yield (r << 16) | (g << 8) | b;
            }
            default -> 0x5599FF; // static blue
        };
    }
}
