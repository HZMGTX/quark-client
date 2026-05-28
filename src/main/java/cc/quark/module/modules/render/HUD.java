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
import cc.quark.util.ColorUtil;
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
 *  - Health, Hunger, Armor, Speed, Coordinates, Effects, FPS, TPS, Combo
 *  - Blink / Ghost badges
 *  - Armor durability bars (bottom-center)
 */
public class HUD extends Module {

    // Layout mode
    private final ModeSetting layout = register(new ModeSetting(
            "Layout", "HUD layout style", "Compact", "Compact", "Expanded", "Minimal"));

    private final ModeSetting colorScheme = register(new ModeSetting(
            "Color", "Color scheme for the HUD text", "Rainbow", "Rainbow", "Static", "Gradient"));

    // Element toggles
    private final BoolSetting watermark      = register(new BoolSetting("Watermark",    "Show client name and version",    true));
    private final BoolSetting showHealth     = register(new BoolSetting("Health",       "Show player health",               true));
    private final BoolSetting showHunger     = register(new BoolSetting("Hunger",       "Show food level",                  true));
    private final BoolSetting showArmorStat  = register(new BoolSetting("Armor",        "Show armor points",                true));
    private final BoolSetting showSpeed      = register(new BoolSetting("Speed",        "Show blocks-per-second",           true));
    private final BoolSetting coordinates    = register(new BoolSetting("Coords",       "Show player coordinates",          true));
    private final BoolSetting potionEffects  = register(new BoolSetting("Effects",      "Show active potion effects",       true));
    private final BoolSetting showFps        = register(new BoolSetting("FPS",          "Show frames-per-second",           true));
    private final BoolSetting showTps        = register(new BoolSetting("TPS",          "Show server TPS estimate",         true));
    private final BoolSetting showCombo      = register(new BoolSetting("Combo",        "Show combo hit counter",           true));

    // Position settings
    public final IntSetting wmX     = register(new IntSetting("Watermark X", "X pos",           5,  0, 3000));
    public final IntSetting wmY     = register(new IntSetting("Watermark Y", "Y pos",           5,  0, 3000));
    public final IntSetting listX   = register(new IntSetting("List X",      "X pos from right", 2, 0, 3000));
    public final IntSetting listY   = register(new IntSetting("List Y",      "Y pos",            5,  0, 3000));
    public final IntSetting coordsX = register(new IntSetting("Coords X",   "X pos",            3,  0, 3000));
    public final IntSetting coordsY = register(new IntSetting("Coords Y",   "Y pos from bottom", 30, 0, 3000));
    public final IntSetting statsX  = register(new IntSetting("Stats X",    "X pos for stats",  3,  0, 3000));
    public final IntSetting statsY  = register(new IntSetting("Stats Y",    "Y pos for stats",  70, 0, 3000));

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
        attackTimestamps.removeIf(t -> now - t > 2000L);
        attackTimestamps.addLast(now);
        combo = attackTimestamps.size();
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        DrawContext ctx   = event.getDrawContext();
        int screenW       = mc.getWindow().getScaledWidth();
        int screenH       = mc.getWindow().getScaledHeight();
        ClientPlayerEntity player = mc.player;
        boolean minimal   = layout.is("Minimal");
        boolean expanded  = layout.is("Expanded");

        // --- TPS tick-timestamp tracking ---
        long now = System.currentTimeMillis();
        tickTimestamps.addLast(now);
        while (tickTimestamps.size() > 20) tickTimestamps.pollFirst();
        if (tickTimestamps.size() >= 2) {
            Long[] ts = tickTimestamps.toArray(new Long[0]);
            long span = ts[ts.length - 1] - ts[0];
            if (span > 0) {
                double avgInterval = (double) span / (ts.length - 1);
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
        if (watermark.isEnabled() && !minimal) {
            String nameText    = "Quark.cc";
            String versionText = " v" + Quark.VERSION;
            int padding        = mc.textRenderer.fontHeight / 2;
            int nameWidth      = mc.textRenderer.getWidth(nameText);
            int versionWidth   = mc.textRenderer.getWidth(versionText);
            int wmWidth        = nameWidth + versionWidth + padding * 2;
            int wmHeight       = mc.textRenderer.fontHeight + padding;
            int renderX        = wmX.getValue();
            int renderY        = wmY.getValue();

            int accentFull  = (cc.quark.gui.ClickGUI.getAccentColor() & 0x00FFFFFF) | 0xAA000000;
            RenderUtil.drawGradientRect(ctx, renderX, renderY, renderX + wmWidth, renderY + wmHeight, accentFull, 0x00000000);
            ctx.fill(renderX, renderY, renderX + wmWidth, renderY + 1, cc.quark.gui.ClickGUI.getAccentColor());

            RenderUtil.drawCustomText(ctx, nameText, renderX + padding, renderY + padding / 2 + 1, 0xFFFFFFFF);
            RenderUtil.drawCustomText(ctx, versionText, renderX + padding + nameWidth, renderY + padding / 2 + 1, 0xFFAAAAAA);
        }

        // ---- Module List (right side) ----
        if (!minimal) {
            List<Module> enabled = Quark.getInstance().getModuleManager().getEnabledModules();
            int yOffset       = listY.getValue();
            int paddingH      = mc.textRenderer.fontHeight / 3;
            int elementHeight = mc.textRenderer.fontHeight + paddingH * 2;
            int rightOffset   = listX.getValue();

            for (int i = enabled.size() - 1; i >= 0; i--) {
                Module mod = enabled.get(i);
                if (!mod.isVisible()) continue;
                if (mod == this) continue;
                String modName  = mod.getName();
                String suffix   = mod.getSuffix();
                String suffixStr = suffix != null ? " [" + suffix + "]" : "";
                int nameWidth   = mc.textRenderer.getWidth(modName);
                int suffixWidth = suffix != null ? mc.textRenderer.getWidth(suffixStr) : 0;
                int textWidth   = nameWidth + suffixWidth;
                int x           = screenW - textWidth - paddingH - rightOffset;
                int color       = getAccentColor((float)(enabled.size() - 1 - i) / Math.max(1, enabled.size() - 1));

                ctx.fill(screenW - textWidth - paddingH * 2 - 2 - rightOffset, yOffset - 1,
                         screenW - rightOffset, yOffset - 1 + elementHeight, 0x88181818);
                ctx.fill(screenW - 2 - rightOffset, yOffset - 1,
                         screenW - rightOffset, yOffset - 1 + elementHeight, color);

                RenderUtil.drawCustomText(ctx, modName, x, yOffset + paddingH, color);
                if (suffix != null) {
                    RenderUtil.drawCustomText(ctx, suffixStr, x + nameWidth, yOffset + paddingH, 0xFF888888);
                }
                yOffset += elementHeight;
            }
        }

        // ---- Stats panel: Health, Hunger, Armor, Speed ----
        int sy = statsY.getValue();
        int sx = statsX.getValue();
        int lineH = mc.textRenderer.fontHeight + 2;

        if (showHealth.isEnabled()) {
            float hp    = player.getHealth();
            float maxHp = player.getMaxHealth();
            float pct   = maxHp > 0 ? hp / maxHp : 1f;
            int hc = cc.quark.util.ColorUtil.healthColor(pct) | 0xFF000000;
            String label = expanded ? String.format("Health: %.1f / %.1f", hp, maxHp)
                                    : String.format("❤ %.1f", hp);
            RenderUtil.drawCustomText(ctx, label, sx, sy, hc);
            sy += lineH;
        }

        if (showHunger.isEnabled()) {
            int food = player.getHungerManager().getFoodLevel();
            int color = food >= 15 ? 0xFF55FF55 : food >= 8 ? 0xFFFFFF55 : 0xFFFF5555;
            String label = expanded ? "Hunger: " + food + "/20" : "🍖 " + food;
            RenderUtil.drawCustomText(ctx, label, sx, sy, color);
            sy += lineH;
        }

        if (showArmorStat.isEnabled()) {
            int armorPoints = player.getArmor();
            int color = armorPoints >= 15 ? 0xFF55FFFF : armorPoints >= 8 ? 0xFFFFFFAA : 0xFFAAAAAA;
            String label = expanded ? "Armor: " + armorPoints : "⛡ " + armorPoints;
            RenderUtil.drawCustomText(ctx, label, sx, sy, color);
            sy += lineH;
        }

        if (showSpeed.isEnabled()) {
            String label = expanded ? String.format("Speed: %.2f BPS", bps)
                                    : String.format("⚡ %.2f", bps);
            RenderUtil.drawCustomText(ctx, label, sx, sy, 0xFFAADDFF);
            sy += lineH;
        }

        // ---- Coordinates / FPS / BPS (bottom-left) ----
        if (coordinates.isEnabled()) {
            int renderX = coordsX.getValue();
            int y       = screenH - coordsY.getValue();
            String coordStr = String.format("XYZ: %.1f / %.1f / %.1f",
                    player.getX(), player.getY(), player.getZ());
            RenderUtil.drawCustomText(ctx, coordStr, renderX, y, 0xFFFFFFFF);

            if (!showSpeed.isEnabled()) {
                String bpsStr = String.format("BPS: %.2f", bps);
                RenderUtil.drawCustomText(ctx, bpsStr, renderX, y + 10, 0xFFAAAAAA);
            }
        }

        // ---- Ping + FPS + TPS (bottom-left stack) ----
        {
            var networkHandler = mc.getNetworkHandler();
            int infoY = screenH - 40;

            if (networkHandler != null) {
                var entry = networkHandler.getPlayerListEntry(player.getUuid());
                if (entry != null) {
                    int ping      = entry.getLatency();
                    int pingColor = ping < 80 ? 0xFF44FF88 : ping < 150 ? 0xFFFFFF44 : 0xFFFF4444;
                    RenderUtil.drawCustomText(ctx, "Ping: " + ping + "ms", 3, infoY, pingColor);
                    infoY -= lineH;
                }
            }

            if (showFps.isEnabled()) {
                int fps      = mc.getCurrentFps();
                int fpsColor = fps >= 60 ? 0xFF44FF88 : fps >= 30 ? 0xFFFFFF44 : 0xFFFF4444;
                RenderUtil.drawCustomText(ctx, "FPS: " + fps, 3, infoY, fpsColor);
                infoY -= lineH;
            }

            if (showTps.isEnabled()) {
                String tpsStr  = String.format("TPS: %.1f", estimatedTps);
                int tpsColor   = estimatedTps >= 19f ? 0xFF44FF88 : estimatedTps >= 15f ? 0xFFFFFF44 : 0xFFFF4444;
                RenderUtil.drawCustomText(ctx, tpsStr, 3, infoY, tpsColor);
            }
        }

        // ---- Combo Counter ----
        if (showCombo.isEnabled() && combo >= 2) {
            String comboStr = "Combo: " + combo;
            int cw = mc.textRenderer.getWidth(comboStr);
            int cx = screenW / 2 - cw / 2;
            int cy = screenH - 80;
            ctx.fill(cx - 4, cy - 2, cx + cw + 4, cy + mc.textRenderer.fontHeight + 2, 0xAA1A1A1A);
            int comboColor = combo >= 10 ? 0xFFFF4444 : combo >= 5 ? 0xFFFFAA00 : 0xFF44FF88;
            RenderUtil.drawCustomText(ctx, comboStr, cx, cy, comboColor);
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
                RenderUtil.drawCustomText(ctx, blinkStr, bx, by, 0xFFFF3333);
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
                RenderUtil.drawCustomText(ctx, ghostStr, gx, gy, accentColor);
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
            RenderUtil.drawCustomText(ctx, names[i].substring(0, 1), x + 13, y + 6, 0xFFCCCCCC);
        }
    }

    private void renderPotionEffects(DrawContext ctx, int screenW, int screenH, ClientPlayerEntity player) {
        int x = 3;
        int y = screenH / 2 - 40;
        int lineH = mc.textRenderer.fontHeight + 2;

        for (StatusEffectInstance effect : player.getStatusEffects()) {
            net.minecraft.entity.effect.StatusEffect eff = effect.getEffectType().value();
            String name  = eff.getName().getString();
            int amp      = effect.getAmplifier();
            int dur      = effect.getDuration() / 20;
            String roman = amp > 0 ? " " + toRoman(amp + 1) : "";
            String durStr = " §7(" + formatDuration(dur) + ")";

            int nameColor;
            if (eff.isBeneficial()) {
                nameColor = 0xFF55FF55;
            } else if (eff.getCategory() == net.minecraft.entity.effect.StatusEffectCategory.NEUTRAL) {
                nameColor = 0xFFFFFFFF;
            } else {
                nameColor = 0xFFFF5555;
            }

            int bgW = mc.textRenderer.getWidth(name + roman + " (" + formatDuration(dur) + ")") + 6;
            ctx.fill(x - 2, y - 1, x + bgW, y + mc.textRenderer.fontHeight + 1, 0x88111111);

            RenderUtil.drawCustomText(ctx, name + roman, x, y, nameColor);
            RenderUtil.drawCustomText(ctx, durStr, x + mc.textRenderer.getWidth(name + roman), y, 0xFF888888);
            y += lineH;
        }
    }

    private String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(num);
        };
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
