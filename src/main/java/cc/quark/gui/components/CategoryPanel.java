package cc.quark.gui.components;

import cc.quark.gui.ClickGUI;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.*;
import cc.quark.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class CategoryPanel {

    private final Category category;
    private final List<Module> modules;
    private int x, y;
    private final int width;
    private static final int HEADER_H = 16;
    private static final int MODULE_H = 12;
    private static final int SETTING_H = 11;

    private boolean dragging = false;
    private int dragOffX, dragOffY;
    private Module expandedModule = null;
    private float scrollOffset = 0;

    public CategoryPanel(Category category, List<Module> modules, int x, int y, int width) {
        this.category = category;
        this.modules = modules;
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public void render(DrawContext ctx, int mx, int my, float delta, String search, float alpha) {
        List<Module> visible = modules.stream()
            .filter(m -> search.isEmpty() || m.getName().toLowerCase().contains(search.toLowerCase()))
            .toList();

        int catColor = ClickGUI.getCategoryColor(category);
        int totalH = HEADER_H + visible.size() * MODULE_H;
        if (expandedModule != null) {
            totalH += expandedModule.getSettings().size() * SETTING_H + 2;
        }

        // Panel background
        ctx.fill(x, y, x + width, y + totalH, ColorUtil.withAlpha(0x111111, (int)(200 * alpha)));
        // Header
        ctx.fill(x, y, x + width, y + HEADER_H, ColorUtil.withAlpha(catColor & 0x00FFFFFF, (int)(180 * alpha)));
        ctx.fill(x, y + HEADER_H - 1, x + width, y + HEADER_H, ColorUtil.withAlpha(catColor & 0x00FFFFFF, 255));
        ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
            category.name(), x + 4, y + 4, catColor);

        int yy = y + HEADER_H;
        for (Module m : visible) {
            boolean hovered = mx >= x && mx <= x + width && my >= yy && my <= yy + MODULE_H;
            int bg = m.isEnabled() ? ColorUtil.withAlpha(catColor & 0x00FFFFFF, (int)(60 * alpha))
                                   : ColorUtil.withAlpha(0x000000, (int)(30 * alpha));
            if (hovered) bg = ColorUtil.withAlpha(0x333333, (int)(150 * alpha));
            ctx.fill(x, yy, x + width, yy + MODULE_H, bg);

            // Enabled indicator bar on left
            if (m.isEnabled()) ctx.fill(x, yy, x + 2, yy + MODULE_H, catColor);

            ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                m.getName(), x + 5, yy + 2, m.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAAA);

            // Keybind
            if (m.getKeybind() != 0) {
                String kb = "[" + org.lwjgl.glfw.GLFW.glfwGetKeyName(m.getKeybind(), 0) + "]";
                ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, kb,
                    x + width - 20, yy + 2, 0xFF666666);
            }
            yy += MODULE_H;

            // Settings panel for expanded module
            if (m == expandedModule) {
                ctx.fill(x, yy, x + width, yy + m.getSettings().size() * SETTING_H + 2, ColorUtil.withAlpha(0x222222, 200));
                int sy = yy + 1;
                for (Setting<?> setting : m.getSettings()) {
                    renderSetting(ctx, setting, x + 3, sy, width - 6, mx, my);
                    sy += SETTING_H;
                }
                yy += m.getSettings().size() * SETTING_H + 2;
            }
        }

        // Border
        ctx.fill(x, y, x + 1, y + totalH, ColorUtil.withAlpha(catColor & 0x00FFFFFF, 100));
        ctx.fill(x + width - 1, y, x + width, y + totalH, ColorUtil.withAlpha(catColor & 0x00FFFFFF, 100));
        ctx.fill(x, y + totalH - 1, x + width, y + totalH, ColorUtil.withAlpha(catColor & 0x00FFFFFF, 100));
    }

    private void renderSetting(DrawContext ctx, Setting<?> setting, int x, int y, int w, int mx, int my) {
        int labelColor = 0xFFCCCCCC;
        ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, setting.getName(), x, y + 1, labelColor);

        if (setting instanceof BoolSetting bs) {
            int toggleX = x + w - 20;
            ctx.fill(toggleX, y + 1, toggleX + 18, y + 9, bs.getValue() ? 0xFF55AA55 : 0xFF553333);
            ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                bs.getValue() ? "ON" : "OFF", toggleX + 2, y + 1, 0xFFFFFFFF);

        } else if (setting instanceof DoubleSetting ds) {
            int barX = x + w/2;
            int barW = w/2;
            double pct = (ds.getValue() - ds.getMin()) / (ds.getMax() - ds.getMin());
            ctx.fill(barX, y + 3, barX + barW, y + 8, 0xFF333333);
            ctx.fill(barX, y + 3, barX + (int)(barW * pct), y + 8, 0xFF5588FF);
            String val = String.format("%.2f", ds.getValue());
            ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, val, barX + barW/2 - 10, y + 1, 0xFFFFFFFF);

        } else if (setting instanceof IntSetting is) {
            int barX = x + w/2;
            int barW = w/2;
            double pct = (double)(is.getValue() - is.getMin()) / (is.getMax() - is.getMin());
            ctx.fill(barX, y + 3, barX + barW, y + 8, 0xFF333333);
            ctx.fill(barX, y + 3, barX + (int)(barW * pct), y + 8, 0xFF5588FF);
            ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, String.valueOf(is.getValue()),
                barX + barW/2 - 5, y + 1, 0xFFFFFFFF);

        } else if (setting instanceof ModeSetting ms) {
            String val = ms.getValue();
            int valX = x + w - MinecraftClient.getInstance().textRenderer.getWidth(val) - 2;
            ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "< " + val + " >", valX - 10, y + 1, 0xFF88AAFF);
        }
    }

    public boolean mouseClicked(int mx, int my, int button) {
        // Header drag
        if (mx >= x && mx <= x + width && my >= y && my <= y + HEADER_H) {
            if (button == 0) { dragging = true; dragOffX = mx - x; dragOffY = my - y; return true; }
        }

        // Module clicks
        List<Module> visible = modules.stream().toList();
        int yy = y + HEADER_H;
        for (Module m : visible) {
            if (mx >= x && mx <= x + width && my >= yy && my <= yy + MODULE_H) {
                if (button == 0) m.toggle();
                if (button == 1) expandedModule = (expandedModule == m) ? null : m;
                return true;
            }
            yy += MODULE_H;
            if (m == expandedModule) {
                for (Setting<?> setting : m.getSettings()) {
                    if (my >= yy && my <= yy + SETTING_H) {
                        handleSettingClick(setting, mx - x - 3, button, x, yy, width - 6);
                        return true;
                    }
                    yy += SETTING_H;
                }
                yy += 2;
            }
        }
        return false;
    }

    private void handleSettingClick(Setting<?> setting, int relX, int button, int absX, int absY, int w) {
        if (setting instanceof BoolSetting bs) {
            if (relX >= w - 20) bs.setValue(!bs.getValue());
        } else if (setting instanceof ModeSetting ms) {
            if (button == 0) ms.next();
            if (button == 1) ms.previous();
        } else if (setting instanceof DoubleSetting ds) {
            int barX = w/2;
            int barW = w/2;
            if (relX >= barX && relX <= barX + barW) {
                double pct = (double)(relX - barX) / barW;
                ds.setValue(ds.getMin() + pct * (ds.getMax() - ds.getMin()));
            }
        } else if (setting instanceof IntSetting is) {
            int barX = w/2;
            int barW = w/2;
            if (relX >= barX && relX <= barX + barW) {
                double pct = (double)(relX - barX) / barW;
                is.setValue((int)(is.getMin() + pct * (is.getMax() - is.getMin())));
            }
        }
    }

    public boolean mouseDragged(int mx, int my, int button, int dx, int dy) {
        if (dragging && button == 0) {
            x = mx - dragOffX;
            y = my - dragOffY;
            return true;
        }
        return false;
    }

    public void mouseReleased() {
        dragging = false;
    }

    public boolean mouseScrolled(int mx, int my, double amount) {
        if (mx >= x && mx <= x + width && my >= y && my <= y + 300) {
            scrollOffset -= (float)amount * 10;
            return true;
        }
        return false;
    }
}
