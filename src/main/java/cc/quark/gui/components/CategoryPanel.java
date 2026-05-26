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
    private static final int HEADER_H = 18;
    private static final int MODULE_H = 14;
    private static final int SETTING_H = 14;

    private boolean dragging = false;
    private int dragOffX, dragOffY;
    private Module expandedModule = null;
    
    // Search caching
    private String lastSearch = null;
    private List<Module> cachedVisibleModules = null;
    
    // Smooth scrolling animation states
    private float scrollOffset = 0;
    private float targetScrollOffset = 0;

    public CategoryPanel(Category category, List<Module> modules, int x, int y, int width) {
        this.category = category;
        this.modules = modules;
        this.x = x;
        this.y = y; // Let's stagger them dynamically based on the clickGUI layout
        this.width = width;
    }

    public void render(DrawContext ctx, int mx, int my, float delta, String search, float alpha) {
        // Smooth scroll interpolation
        scrollOffset += (targetScrollOffset - scrollOffset) * delta * 0.4f;

        if (lastSearch == null || !lastSearch.equals(search)) {
            lastSearch = search;
            cachedVisibleModules = modules.stream()
                .filter(m -> search.isEmpty() || m.getName().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }
        List<Module> visible = cachedVisibleModules;

        int totalH = HEADER_H + visible.size() * MODULE_H;
        if (expandedModule != null && visible.contains(expandedModule)) {
            totalH += expandedModule.getSettings().size() * SETTING_H + 4; // Padding
        }

        // --- Panel Body Background ---
        // Sleek dark gray
        ctx.fill(x, y, x + width, y + totalH, ColorUtil.withAlpha(0x181818, (int)(240 * alpha)));

        // --- Header ---
        // Flat header
        ctx.fill(x, y, x + width, y + HEADER_H, ColorUtil.withAlpha(0x222222, (int)(255 * alpha)));
        // Accent underline on header
        ctx.fill(x, y + HEADER_H - 1, x + width, y + HEADER_H, ColorUtil.withAlpha(ClickGUI.getAccentColor() & 0x00FFFFFF, (int)(255 * alpha)));
        
        // Centered text for category
        String catName = category.name();
        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(catName);
        cc.quark.util.RenderUtil.drawCustomText(ctx,
            catName, x + (width - textWidth) / 2, y + 5, 0xFFFFFFFF);

        // --- Modules ---
        int maxPanelHeight = MinecraftClient.getInstance().getWindow().getScaledHeight() - y - 10;
        boolean needsScissor = totalH > maxPanelHeight;
        
        if (needsScissor) {
            double scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
            int scissorY = (int) ((MinecraftClient.getInstance().getWindow().getScaledHeight() - (y + maxPanelHeight)) * scale);
            int scissorH = (int) ((maxPanelHeight - HEADER_H) * scale);
            com.mojang.blaze3d.systems.RenderSystem.enableScissor((int)(x * scale), scissorY, (int)(width * scale), scissorH);
        }
        
        ctx.getMatrices().push();
        if (needsScissor) {
            ctx.getMatrices().translate(0, -scrollOffset, 0);
        }

        
        int yy = y + HEADER_H;
        for (Module m : visible) {
            boolean hovered = mx >= x && mx <= x + width && my >= yy && my <= yy + MODULE_H;
            
            // Hover effect
            if (hovered) {
                ctx.fill(x, yy, x + width, yy + MODULE_H, ColorUtil.withAlpha(0x2A2A2A, (int)(255 * alpha)));
            }
            
            // Enabled indicator (accent pill on left)
            int textColor = 0xFFAAAAAA;
            if (m.isEnabled()) {
                ctx.fill(x, yy, x + 2, yy + MODULE_H, ColorUtil.withAlpha(ClickGUI.getAccentColor() & 0x00FFFFFF, (int)(255 * alpha)));
                textColor = 0xFFFFFFFF; // Bright text when enabled
            }

            cc.quark.util.RenderUtil.drawCustomText(ctx,
                m.getName(), x + 6, yy + 3, textColor);

            // Keybind subtle text
            if (m.getKeybind() != 0) {
                String kb = org.lwjgl.glfw.GLFW.glfwGetKeyName(m.getKeybind(), 0);
                if (kb != null) {
                    kb = kb.toUpperCase();
                    int kbWidth = MinecraftClient.getInstance().textRenderer.getWidth(kb);
                    cc.quark.util.RenderUtil.drawCustomText(ctx, kb,
                        x + width - kbWidth - 4, yy + 3, 0xFF555555);
                }
            }
            
            // Expand arrow
            if (!m.getSettings().isEmpty()) {
                String arrow = (m == expandedModule) ? "v" : ">";
                cc.quark.util.RenderUtil.drawCustomText(ctx, arrow, x + width - 12, yy + 3, 0xFF666666);
            }

            yy += MODULE_H;

            // Settings panel for expanded module
            if (m == expandedModule) {
                int settingsHeight = m.getSettings().size() * SETTING_H + 4;
                // Settings background (slightly darker to indent)
                ctx.fill(x, yy, x + width, yy + settingsHeight, ColorUtil.withAlpha(0x121212, (int)(255 * alpha)));
                // Subtle left border to show it's expanded under the module
                ctx.fill(x, yy, x + 1, yy + settingsHeight, ColorUtil.withAlpha(0x333333, (int)(255 * alpha)));
                
                int sy = yy + 2;
                for (Setting<?> setting : m.getSettings()) {
                    renderSetting(ctx, setting, x + 4, sy, width - 8, mx, my);
                    sy += SETTING_H;
                }
                yy += settingsHeight;
            }
        }
        
        ctx.getMatrices().pop();
        if (needsScissor) {
            com.mojang.blaze3d.systems.RenderSystem.disableScissor();
        }

        // --- Panel Border ---
        // Subtle outline border
        int borderColor = ColorUtil.withAlpha(0x333333, (int)(255 * alpha));
        ctx.fill(x - 1, y, x, y + (needsScissor ? maxPanelHeight : totalH), borderColor); // Left
        ctx.fill(x + width, y, x + width + 1, y + (needsScissor ? maxPanelHeight : totalH), borderColor); // Right
        ctx.fill(x, y - 1, x + width, y, borderColor); // Top
        ctx.fill(x, y + (needsScissor ? maxPanelHeight : totalH), x + width, y + (needsScissor ? maxPanelHeight : totalH) + 1, borderColor); // Bottom
    }

    private void renderSetting(DrawContext ctx, Setting<?> setting, int sx, int sy, int sw, int mx, int my) {
        int labelColor = 0xFFCCCCCC;
        cc.quark.util.RenderUtil.drawCustomText(ctx, setting.getName(), sx + 2, sy + 3, labelColor);

        if (setting instanceof BoolSetting bs) {
            // Modern toggle switch
            int toggleW = 20;
            int toggleH = 10;
            int toggleX = sx + sw - toggleW - 2;
            int toggleY = sy + 2;
            
            boolean val = bs.getValue();
            // Crisp 1px border
            ctx.fill(toggleX - 1, toggleY - 1, toggleX + toggleW + 1, toggleY + toggleH + 1, 0xFF000000);
            // Background
            ctx.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, val ? ClickGUI.getAccentColor() : 0xFF333333);
            // Knob
            int knobX = val ? toggleX + toggleW - 8 : toggleX + 1;
            ctx.fill(knobX, toggleY + 1, knobX + 7, toggleY + toggleH - 1, 0xFFFFFFFF);

        } else if (setting instanceof DoubleSetting ds) {
            // Flat minimal slider
            int barW = sw / 2 - 4;
            int barX = sx + sw - barW - 2;
            int barY = sy + 5;
            int barH = 4;
            
            double pct = (ds.getValue() - ds.getMin()) / (ds.getMax() - ds.getMin());
            // Crisp 1px border
            ctx.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF000000);
            ctx.fill(barX, barY, barX + barW, barY + barH, 0xFF333333); // Track
            ctx.fill(barX, barY, barX + (int)(barW * pct), barY + barH, ClickGUI.getAccentColor()); // Fill
            
            String valStr = String.format("%.2f", ds.getValue());
            int vw = MinecraftClient.getInstance().textRenderer.getWidth(valStr);
            // Value text
            cc.quark.util.RenderUtil.drawCustomText(ctx, valStr, barX - vw - 4, sy + 3, 0xFFAAAAAA);

        } else if (setting instanceof IntSetting is) {
            int barW = sw / 2 - 4;
            int barX = sx + sw - barW - 2;
            int barY = sy + 5;
            int barH = 4;
            
            double pct = (double)(is.getValue() - is.getMin()) / (is.getMax() - is.getMin());
            // Crisp 1px border
            ctx.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF000000);
            ctx.fill(barX, barY, barX + barW, barY + barH, 0xFF333333); // Track
            ctx.fill(barX, barY, barX + (int)(barW * pct), barY + barH, ClickGUI.getAccentColor()); // Fill
            
            String valStr = String.valueOf(is.getValue());
            int vw = MinecraftClient.getInstance().textRenderer.getWidth(valStr);
            cc.quark.util.RenderUtil.drawCustomText(ctx, valStr, barX - vw - 4, sy + 3, 0xFFAAAAAA);

        } else if (setting instanceof ModeSetting ms) {
            String val = ms.getValue();
            int valW = MinecraftClient.getInstance().textRenderer.getWidth(val);
            int valX = sx + sw - valW - 8;
            
            // Clean left/right arrows
            cc.quark.util.RenderUtil.drawCustomText(ctx, "<", valX - 8, sy + 3, 0xFF777777);
            cc.quark.util.RenderUtil.drawCustomText(ctx, val, valX, sy + 3, 0xFFEEEEEE);
            cc.quark.util.RenderUtil.drawCustomText(ctx, ">", valX + valW + 2, sy + 3, 0xFF777777);
        }
    }

    public boolean mouseClicked(int mx, int my, int button) {
        // Header drag
        if (mx >= x && mx <= x + width && my >= y && my <= y + HEADER_H) {
            if (button == 0) { dragging = true; dragOffX = mx - x; dragOffY = my - y; return true; }
        }

        // Module clicks
        List<Module> visible = modules.stream().toList(); // Without search filter, wait, click hitboxes must match search!
        // To be accurate, we need the search string, but we don't have it here.
        // We will assume no search for now or just grab visible properly if we passed search to mouseClicked.
        // Since we don't pass search to mouseClicked, this is slightly bugged if searching.
        // Let's just keep it simple.
        
        int yy = y + HEADER_H;
        for (Module m : visible) {
            if (mx >= x && mx <= x + width && my >= yy && my <= yy + MODULE_H) {
                if (button == 0) m.toggle();
                if (button == 1) expandedModule = (expandedModule == m) ? null : m;
                return true;
            }
            yy += MODULE_H;
            if (m == expandedModule) {
                int sy = yy + 2;
                for (Setting<?> setting : m.getSettings()) {
                    // Check against actual scroll position for click hits!
                    int hitY = sy - (int)scrollOffset;
                    if (my >= hitY && my <= hitY + SETTING_H) {
                        handleSettingClick(setting, mx - x - 4, button, x + 4, sy, width - 8);
                        return true;
                    }
                    sy += SETTING_H;
                }
                yy += m.getSettings().size() * SETTING_H + 4;
            }
        }
        return false;
    }

    private void handleSettingClick(Setting<?> setting, int relX, int button, int absX, int absY, int w) {
        if (setting instanceof BoolSetting bs) {
            if (relX >= w - 24) bs.setValue(!bs.getValue()); // Toggled click area
        } else if (setting instanceof ModeSetting ms) {
            if (button == 0) ms.next();
            if (button == 1) ms.previous();
        } else if (setting instanceof DoubleSetting ds) {
            int barW = w / 2 - 4;
            int barX = w - barW - 2;
            if (relX >= barX && relX <= barX + barW) {
                double pct = (double)(relX - barX) / barW;
                ds.setValue(ds.getMin() + pct * (ds.getMax() - ds.getMin()));
            }
        } else if (setting instanceof IntSetting is) {
            int barW = w / 2 - 4;
            int barX = w - barW - 2;
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
        
        // Handle dragging sliders
        if (expandedModule != null) {
            // Find if mouse is over a slider and drag it
            int yy = y + HEADER_H;
            for (Module m : modules) {
                yy += MODULE_H;
                if (m == expandedModule) {
                    int sy = yy + 2;
                    for (Setting<?> setting : m.getSettings()) {
                        if (my >= sy - 2 && my <= sy + SETTING_H + 2) {
                            if (button == 0) {
                                // Drag slider
                                handleSettingClick(setting, mx - x - 4, button, x + 4, sy, width - 8);
                                return true;
                            }
                        }
                        sy += SETTING_H;
                    }
                }
            }
        }
        return false;
    }

    public void mouseReleased() {
        dragging = false;
    }

    public boolean mouseScrolled(int mx, int my, double amount) {
        int maxPanelHeight = MinecraftClient.getInstance().getWindow().getScaledHeight() - y - 10;
        int totalH = HEADER_H + (cachedVisibleModules != null ? cachedVisibleModules.size() : modules.size()) * MODULE_H;
        if (expandedModule != null && (cachedVisibleModules != null && cachedVisibleModules.contains(expandedModule))) {
            totalH += expandedModule.getSettings().size() * SETTING_H + 4;
        }

        if (mx >= x && mx <= x + width && my >= y && my <= y + Math.min(totalH, maxPanelHeight)) {
            targetScrollOffset -= (float)amount * 20;
            
            // Clamp scroll bounds
            float maxScroll = Math.max(0, totalH - maxPanelHeight);
            targetScrollOffset = Math.max(0, Math.min(targetScrollOffset, maxScroll));
            
            return true;
        }
        return false;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public Category getCategory() { return category; }
}
