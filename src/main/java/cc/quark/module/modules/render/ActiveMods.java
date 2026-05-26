package cc.quark.module.modules.render;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveMods extends Module {

    private final ModeSetting side = register(new ModeSetting(
            "Side", "Which side to render on", "Left", "Left", "Right"));

    private final IntSetting xOffset = register(new IntSetting(
            "X Offset", "Pixels from screen edge", 4, 0, 50));

    private final IntSetting startY = register(new IntSetting(
            "Y", "Starting Y position", 4, 0, 500));

    private final BoolSetting background = register(new BoolSetting(
            "Background", "Draw per-entry background", true));

    private final BoolSetting categoryDots = register(new BoolSetting(
            "Category Dots", "Show colored category indicator dots", true));

    private final ModeSetting colorMode = register(new ModeSetting(
            "Color", "Entry color mode", "Rainbow", "Rainbow", "Accent", "White"));

    private final Map<String, Float> slideProgress = new HashMap<>();

    public ActiveMods() {
        super("ActiveMods", "Shows enabled modules with category color indicators", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.getWindow() == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int fontH = mc.textRenderer.fontHeight;
        int padV = 1;
        int padH = 4;
        int dotSize = 4;
        int dotPad = 3;
        int elementH = fontH + padV * 2;
        boolean left = side.is("Left");

        List<Module> enabled = Quark.getInstance().getModuleManager().getModules().stream()
                .filter(m -> m.isEnabled() && m.isVisible() && m != this)
                .sorted(Comparator.comparingInt((Module m) ->
                        mc.textRenderer.getWidth(m.getName())).reversed())
                .toList();

        for (String key : new java.util.HashSet<>(slideProgress.keySet())) {
            boolean still = enabled.stream().anyMatch(m -> m.getName().equals(key));
            if (!still) {
                float p = slideProgress.getOrDefault(key, 1f) - 0.12f;
                if (p <= 0f) slideProgress.remove(key);
                else slideProgress.put(key, p);
            }
        }
        for (Module m : enabled) {
            float cur = slideProgress.getOrDefault(m.getName(), 0f);
            cur = Math.min(1f, cur + 0.12f);
            slideProgress.put(m.getName(), cur);
        }

        int y = startY.get();
        int i = 0;
        for (Module mod : enabled) {
            String name = mod.getName();
            int nameWidth = mc.textRenderer.getWidth(name);
            int extraLeft = (categoryDots.isEnabled() && left) ? dotSize + dotPad : 0;
            int extraRight = (categoryDots.isEnabled() && !left) ? dotSize + dotPad : 0;
            int totalWidth = nameWidth + padH * 2 + extraLeft + extraRight;

            float slide = slideProgress.getOrDefault(name, 1f);
            int slideOffset = (int)((1f - slide) * (totalWidth + 4));

            int color = getEntryColor(i, enabled.size());
            int dotColor = getCategoryColor(mod);

            int drawX;
            if (left) {
                drawX = xOffset.get() + extraLeft - slideOffset;
            } else {
                drawX = sw - nameWidth - xOffset.get() - extraRight + slideOffset;
            }

            if (background.isEnabled()) {
                int bgX1, bgX2;
                if (left) {
                    bgX1 = xOffset.get();
                    bgX2 = drawX + nameWidth + padH;
                } else {
                    bgX1 = drawX - padH;
                    bgX2 = sw - xOffset.get();
                }
                ctx.fill(bgX1, y - padV, bgX2, y + fontH + padV, 0x88181818);
                if (left) {
                    ctx.fill(bgX1, y - padV, bgX1 + 2, y + fontH + padV, color);
                } else {
                    ctx.fill(bgX2 - 2, y - padV, bgX2, y + fontH + padV, color);
                }
            }

            if (categoryDots.isEnabled()) {
                int dotX = left ? (xOffset.get() - slideOffset + 2) : (drawX + nameWidth + padH / 2);
                int dotY = y + (fontH - dotSize) / 2;
                ctx.fill(dotX, dotY, dotX + dotSize, dotY + dotSize, dotColor);
            }

            ctx.drawTextWithShadow(mc.textRenderer, name, drawX, y, color);

            String suffix = mod.getSuffix();
            if (suffix != null) {
                ctx.drawTextWithShadow(mc.textRenderer, " " + suffix, drawX + nameWidth, y, 0xFF888888);
            }

            y += elementH;
            i++;
        }
    }

    private int getEntryColor(int index, int total) {
        return switch (colorMode.get()) {
            case "Rainbow" -> ColorUtil.rainbow((System.currentTimeMillis() % 2000L) / 2000f * 360f + index * 30f);
            case "Accent" -> cc.quark.gui.ClickGUI.getAccentColor();
            case "White" -> 0xFFFFFFFF;
            default -> cc.quark.gui.ClickGUI.getAccentColor();
        };
    }

    private int getCategoryColor(Module mod) {
        return switch (mod.getCategory()) {
            case COMBAT -> 0xFFFF4444;
            case MOVEMENT -> 0xFF44FF44;
            case RENDER -> 0xFF4488FF;
            case PLAYER -> 0xFFFFFF44;
            case WORLD -> 0xFFFF8844;
            case EXPLOIT -> 0xFFFF44FF;
            case MISC -> 0xFFAAAAAA;
        };
    }
}
