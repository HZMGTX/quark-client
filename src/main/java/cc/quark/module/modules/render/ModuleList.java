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

public class ModuleList extends Module {

    private final ModeSetting side = register(new ModeSetting(
            "Side", "Which side to render on", "Right", "Right", "Left"));

    private final IntSetting xOffset = register(new IntSetting(
            "X Offset", "Pixels from screen edge", 2, 0, 50));

    private final IntSetting startY = register(new IntSetting(
            "Y", "Starting Y position", 2, 0, 500));

    private final BoolSetting background = register(new BoolSetting(
            "Background", "Draw per-entry background", true));

    private final BoolSetting brackets = register(new BoolSetting(
            "Brackets", "Show brackets around name", false));

    private final ModeSetting colorMode = register(new ModeSetting(
            "Color", "Entry color mode", "Rainbow", "Rainbow", "Accent", "White", "Custom"));

    private final Map<String, Float> slideProgress = new HashMap<>();

    public ModuleList() {
        super("ModuleList", "Lists enabled modules on the screen", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.getWindow() == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int fontH = mc.textRenderer.fontHeight;
        int padV = 1;
        int padH = 4;
        int elementH = fontH + padV * 2;
        boolean right = side.is("Right");

        List<Module> enabled = Quark.getInstance().getModuleManager().getModules().stream()
                .filter(m -> m.isEnabled() && m.isVisible() && m != this)
                .sorted(Comparator.comparingInt((Module m) -> {
                    String display = buildDisplay(m);
                    return mc.textRenderer.getWidth(display);
                }).reversed())
                .toList();

        for (String key : new java.util.HashSet<>(slideProgress.keySet())) {
            boolean stillEnabled = enabled.stream().anyMatch(m -> m.getName().equals(key));
            if (!stillEnabled) {
                float p = slideProgress.getOrDefault(key, 1f) - 0.12f;
                if (p <= 0f) slideProgress.remove(key);
                else slideProgress.put(key, p);
            }
        }
        for (Module m : enabled) {
            String key = m.getName();
            float cur = slideProgress.getOrDefault(key, 0f);
            cur = Math.min(1f, cur + 0.12f);
            slideProgress.put(key, cur);
        }

        int y = startY.get();
        int i = 0;
        for (Module mod : enabled) {
            String name = mod.getName();
            String suffix = mod.getSuffix();
            String nameDisplay = brackets.isEnabled() ? "[" + name + "]" : name;
            String suffixDisplay = suffix != null ? " " + suffix : "";
            int nameWidth = mc.textRenderer.getWidth(nameDisplay);
            int suffixWidth = mc.textRenderer.getWidth(suffixDisplay);
            int totalWidth = nameWidth + suffixWidth + padH * 2;

            float slide = slideProgress.getOrDefault(name, 1f);
            int slideOffset = (int)((1f - slide) * (totalWidth + 4));

            int color = getEntryColor(i, enabled.size());

            int drawX;
            if (right) {
                drawX = sw - totalWidth - xOffset.get() + slideOffset;
            } else {
                drawX = xOffset.get() - slideOffset;
            }

            if (background.isEnabled()) {
                int bgX1, bgX2;
                if (right) {
                    bgX1 = drawX - padH;
                    bgX2 = sw - xOffset.get();
                } else {
                    bgX1 = xOffset.get();
                    bgX2 = drawX + totalWidth + padH;
                }
                ctx.fill(bgX1, y - padV, bgX2, y + fontH + padV, 0x88181818);
                if (right) {
                    ctx.fill(bgX2 - 2, y - padV, bgX2, y + fontH + padV, color);
                } else {
                    ctx.fill(bgX1, y - padV, bgX1 + 2, y + fontH + padV, color);
                }
            }

            ctx.drawTextWithShadow(mc.textRenderer, nameDisplay, drawX, y, color);
            if (suffix != null) {
                ctx.drawTextWithShadow(mc.textRenderer, suffixDisplay, drawX + nameWidth, y, 0xFF888888);
            }

            y += elementH;
            i++;
        }
    }

    private String buildDisplay(Module m) {
        String name = brackets.isEnabled() ? "[" + m.getName() + "]" : m.getName();
        String suffix = m.getSuffix();
        return suffix != null ? name + " " + suffix : name;
    }

    private int getEntryColor(int index, int total) {
        return switch (colorMode.get()) {
            case "Rainbow" -> ColorUtil.rainbow((System.currentTimeMillis() % 2000L) / 2000f * 360f + index * 30f);
            case "Accent" -> cc.quark.gui.ClickGUI.getAccentColor();
            case "White" -> 0xFFFFFFFF;
            default -> cc.quark.gui.ClickGUI.getAccentColor();
        };
    }
}
