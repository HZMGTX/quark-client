package cc.quark.module.modules.render;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.modules.misc.AntiDetect;
import cc.quark.module.modules.player.Blink;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * HUD - main heads-up display overlay.
 *
 * Renders:
 *  - Client watermark (top-left)
 *  - Enabled modules list (right side, sorted by name length descending)
 *  - Armor durability (bottom-center)
 *  - Coordinates / FPS / Ping (bottom-left)
 *  - Speed / BPS (rolling 5-tick average)
 *  - Active potion effects
 *  - Server TPS estimate (from tick timestamps)
 *  - Combo counter (attacks within 2s)
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

    // Position settings (saved automatically)
    public final IntSetting wmX    = register(new IntSetting("Watermark X", "X pos", 5, 0, 3000));
    public final IntSetting wmY    = register(new IntSetting("Watermark Y", "Y pos", 5, 0, 3000));
    public final IntSetting listX  = register(new IntSetting("List X", "X pos from right", 2, 0, 3000));
    public final IntSetting listY  = register(new IntSetting("List Y", "Y pos", 5, 0, 3000));
    public final IntSetting coordsX = register(new IntSetting("Coords X", "X pos", 3, 0, 3000));
    public final IntSetting coordsY = register(new IntSetting("Coords Y", "Y pos from bottom", 30, 0, 3000));

    // Rolling BPS: last 5 tick positions
    private final Deque<Double> bpsHistory = new ArrayDeque<>();
    private double prevX, prevZ;
    private double bps;

    // TPS estimation: track last 20 tick timestamps (ms)
    private final Deque<Long> tickTimestamps = new ArrayDeque<>();
    private float estimatedTps = 20f;

    // Combo counter: track attack timestamps
    private final Deque<Long> attackTimestamps = new ArrayDeque<>();
    private int combo = 0;

    public HUD() {
        super("HUD", "Main HUD overlay with module list and player info", Category.RENDER);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        long now = System.currentTimeMillis();
        // Remove attacks older than 2 seconds
        attackTimestamps.removeIf(t -> now - t > 2000L);
        attackTimestamps.addLast(now);
        combo = attackTimestamps.size();
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        DrawContext ctx  = event.getDrawContext();
        int screenW      = mc.getWindow().getScaledWidth();
        int screenH      = mc.getWindow().getScaledHeight();
        ClientPlayerEntity player = mc.player;

        // --- TPS tick-timestamp tracking ---
        long now = System.currentTimeMillis();
        tickTimestamps.addLast(now);
        while (tickTimestamps.size() > 20) tickTimestamps.pollFirst();
        if (tickTimestamps.size() >= 2) {
            Long[] ts = tickTimestamps.toArray(new Long[0]);
            long span = ts[ts.length - 1] - ts[0];
            if (span > 0) {
                double avgInterval = (double) span / (ts.length - 1); // ms per tick
                estimatedTps = (float) Math.min(20.0, 1000.0 / avgInterval);
            }
        }

        // --- Rolling BPS (5-tick average) ---
        double dx = player.getX() - prevX;
        double dz = player.getZ() - prevZ;
        double tickSpeed = Math.sqrt(dx * dx + dz * dz) * 20.0;
        prevX = player.getX();
        prevZ = player.getZ();

        bpsHistory.addLast(tickSpeed);
        while (bpsHistory.size() > 5) bpsHistory.pollFirst();
        bps = bpsHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        // --- Combo expiry ---
        attackTimestamps.removeIf(t -> now - t > 2000L);
        combo = attackTimestamps.size();

        // ---- Watermark ----
        if (watermark.isEnabled()) {
            String wmText = "Quark.cc §7v" + Quark.VERSION;
            int padding   = mc.textRenderer.fontHeight / 2;
            int wmWidth   = mc.textRenderer.getWidth(wmText) + padding * 2;
            int wmHeight  = mc.textRenderer.fontHeight + padding;
            int renderX   = wmX.getValue();
            int renderY   = wmY.getValue();

            ctx.fill(renderX, renderY, renderX + wmWidth, renderY + wmHeight, 0xAA181818);
            ctx.fill(renderX, renderY, renderX + wmWidth, renderY + 1, cc.quark.gui.ClickGUI.getAccentColor());

            cc.quark.util.RenderUtil.drawCustomText(ctx, wmText, renderX + padding, renderY + padding / 2 + 1, 0xFFFFFFFF);
        }

        // ---- Module List (right side) ----
        if (moduleList.isEnabled()) {
            List<Module> enabled = Quark.getInstance().getModuleManager().getEnabledModules();
            int yOffset     = listY.getValue();
            int paddingH    = mc.textRenderer.fontHeight / 3;
            int elementHeight = mc.textRenderer.fontHeight + paddingH * 2;
            int rightOffset = listX.getValue();

            for (int i = enabled.size() - 1; i >= 0; i--) {
                Module mod = enabled.get(i);
                if (!mod.isVisible()) continue;
                if (mod == this) continue;
                String modName  = mod.getName();
                int textWidth   = mc.textRenderer.getWidth(modName);
                int x           = screenW - textWidth - paddingH - rightOffset;
                int color       = getAccentColor((float)(enabled.size() - 1 - i) / Math.max(1, enabled.size() - 1));

                ctx.fill(screenW - textWidth - paddingH * 2 - 2 - rightOffset, yOffset - 1,
                         screenW - rightOffset, yOffset - 1 + elementHeight, 0x88181818);
                ctx.fill(screenW - 2 - rightOffset, yOffset - 1,
                         screenW - rightOffset, yOffset - 1 + elementHeight, color);

                cc.quark.util.RenderUtil.drawCustomText(ctx, modName, x, yOffset + paddingH, color);
                yOffset += elementHeight;
            }
        }

        // ---- Coordinates / FPS / BPS (bottom-left) ----
        if (coordinates.isEnabled()) {
            int renderX = coordsX.getValue();
            int y       = screenH - coordsY.getValue();
            String coordStr = String.format("XYZ: %.1f / %.1f / %.1f",
                    player.getX(), player.getY(), player.getZ());
            cc.quark.util.RenderUtil.drawCustomText(ctx, coordStr, renderX, y, 0xFFFFFFFF);

            String bpsStr = String.format("BPS: %.2f", bps);
            cc.quark.util.RenderUtil.drawCustomText(ctx, bpsStr, renderX, y + 10, 0xFFAAAAAA);
        }

        // ---- Ping + FPS (left side, above coords) ----
        {
            var networkHandler = mc.getNetworkHandler();
            if (networkHandler != null) {
                var entry = networkHandler.getPlayerListEntry(player.getUuid());
                if (entry != null) {
                    int ping      = entry.getLatency();
                    int pingColor = ping < 80 ? 0xFF44FF88 : ping < 150 ? 0xFFFFFF44 : 0xFFFF4444;
                    cc.quark.util.RenderUtil.drawCustomText(ctx, "Ping: " + ping + "ms", 3, screenH - 40, pingColor);
                }
            }

            int fps      = mc.getCurrentFps();
            int fpsColor = fps >= 60 ? 0xFF44FF88 : fps >= 30 ? 0xFFFFFF44 : 0xFFFF4444;
            cc.quark.util.RenderUtil.drawCustomText(ctx, "FPS: " + fps, 3, screenH - 50, fpsColor);

            // Real estimated TPS
            String tpsStr   = String.format("TPS: %.1f", estimatedTps);
            int tpsColor    = estimatedTps >= 19f ? 0xFF44FF88 : estimatedTps >= 15f ? 0xFFFFFF44 : 0xFFFF4444;
            cc.quark.util.RenderUtil.drawCustomText(ctx, tpsStr, 3, screenH - 60, tpsColor);
        }

        // ---- Combo Counter ----
        if (combo >= 2) {
            String comboStr = "Combo: " + combo;
            int cw = mc.textRenderer.getWidth(comboStr);
            int cx = screenW / 2 - cw / 2;
            int cy = screenH - 80;
            ctx.fill(cx - 4, cy - 2, cx + cw + 4, cy + mc.textRenderer.fontHeight + 2, 0xAA1A1A1A);
            int comboColor = combo >= 10 ? 0xFFFF4444 : combo >= 5 ? 0xFFFFAA00 : 0xFF44FF88;
            cc.quark.util.RenderUtil.drawCustomText(ctx, comboStr, cx, cy, comboColor);
        }

        // ---- Blink packet queue indicator ----
        {
            Blink blink = Quark.getInstance().getModuleManager().getModule(Blink.class);
            if (blink != null && blink.isEnabled()) {
                int queued   = blink.getBufferSize();
                String blinkStr = "BLINK [" + queued + "]";
                int blinkW   = mc.textRenderer.getWidth(blinkStr);
                int bx       = screenW / 2 - blinkW / 2;
                int by       = screenH - 55;
                ctx.fill(bx - 4, by - 2, bx + blinkW + 4, by + mc.textRenderer.fontHeight + 2, 0xAA2A0000);
                cc.quark.util.RenderUtil.drawCustomText(ctx, blinkStr, bx, by, 0xFFFF3333);
            }
        }

        // ---- GHOST badge (shown when AntiDetect is enabled) ----
        {
            AntiDetect antiDetect = Quark.getInstance().getModuleManager().getModule(AntiDetect.class);
            if (antiDetect != null && antiDetect.isEnabled()) {
                int accentColor = cc.quark.gui.ClickGUI.getAccentColor();
                String ghostStr = "  GHOST  ";
                int gw = mc.textRenderer.getWidth(ghostStr);
                int gx = screenW - gw - 6;
                int gy = 6;
                ctx.fill(gx - 2, gy - 2, gx + gw + 2, gy + mc.textRenderer.fontHeight + 2, 0xBB111111);
                ctx.fill(gx - 2, gy - 2, gx + gw + 2, gy - 1, accentColor);
                cc.quark.util.RenderUtil.drawCustomText(ctx, ghostStr, gx, gy, accentColor);
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
        String[] names   = {"Boots", "Legs", "Chest", "Helm"};
        int startX       = screenW / 2 - 80;
        int y            = screenH - 22;

        for (int i = 0; i < 4; i++) {
            ItemStack armor = player.getInventory().getStack(armorSlots[i]);
            if (armor.isEmpty() || !armor.isDamageable()) continue;

            int maxDmg   = armor.getMaxDamage();
            int dmg      = armor.getDamage();
            float pct    = (float)(maxDmg - dmg) / maxDmg;
            int barW     = 30;
            int filledW  = (int)(barW * pct);
            int barColor = pct > 0.6f ? 0xFF00FF44 : pct > 0.3f ? 0xFFFFFF00 : 0xFFFF2222;

            int x = startX + i * 40;
            ctx.fill(x, y, x + barW, y + 4, 0x88000000);
            ctx.fill(x, y, x + filledW, y + 4, barColor);
            cc.quark.util.RenderUtil.drawCustomText(ctx, names[i].substring(0, 1),
                    x + 13, y + 6, 0xFFCCCCCC);
        }
    }

    private void renderPotionEffects(DrawContext ctx, int screenW, int screenH, ClientPlayerEntity player) {
        int x = 3;
        int y = screenH / 2 - 40;

        for (StatusEffectInstance effect : player.getStatusEffects()) {
            String name = effect.getEffectType().value().getName().getString();
            int amp      = effect.getAmplifier() + 1;
            int dur      = effect.getDuration() / 20;
            String line  = name + (amp > 1 ? " " + amp : "")
                    + " §7(" + formatDuration(dur) + ")§r";
            cc.quark.util.RenderUtil.drawCustomText(ctx, line, x, y, 0xFFFFFFFF);
            y += 11;
        }
    }

    private String formatDuration(int seconds) {
        if (seconds > 9999) return "**:**";
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    private int getAccentColor(float t) {
        return switch (colorScheme.get()) {
            case "Rainbow" -> {
                float hue = (System.currentTimeMillis() % 2000L) / 2000.0f + t * 0.15f;
                yield Color.HSBtoRGB(hue % 1.0f, 1.0f, 1.0f);
            }
            case "Gradient" -> {
                int r = (int)(0x80 + t * 0x7F);
                int g = 0x00;
                int b = (int)(0xFF - t * 0x7F);
                yield (r << 16) | (g << 8) | b;
            }
            default -> cc.quark.gui.ClickGUI.getAccentColor();
        };
    }
}
