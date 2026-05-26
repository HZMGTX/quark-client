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

    // Expand/collapse animation
    private boolean expanded = true;
    private float panelHeight = -1f; // -1 means uninitialized
    private long lastClickTime = 0;

    // Right-click context menu state
    private Module contextMenuModule = null;
    private int contextMenuX = 0;
    private int contextMenuY = 0;
    private static final String[] CONTEXT_OPTIONS = {"Toggle", "Bind key", "Info"};

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

        int fullTotalH = HEADER_H + visible.size() * MODULE_H;
        if (expandedModule != null && visible.contains(expandedModule)) {
            fullTotalH += expandedModule.getSettings().size() * SETTING_H + 4;
        }

        // Smooth collapse animation: lerp panelHeight toward target
        float targetH = expanded ? fullTotalH : HEADER_H;
        if (panelHeight < 0) panelHeight = targetH; // initialize
        panelHeight += (targetH - panelHeight) * delta * 0.3f;
        // Snap when very close
        if (Math.abs(panelHeight - targetH) < 0.5f) panelHeight = targetH;
        int animatedH = (int) panelHeight;

        // --- Panel Body Background ---
        ctx.fill(x, y, x + width, y + animatedH, ColorUtil.withAlpha(0x181818, (int)(240 * alpha)));

        // --- Header ---
        ctx.fill(x, y, x + width, y + HEADER_H, ColorUtil.withAlpha(0x222222, (int)(255 * alpha)));
        // Accent underline on header
        ctx.fill(x, y + HEADER_H - 1, x + width, y + HEADER_H, ColorUtil.withAlpha(ClickGUI.getAccentColor() & 0x00FFFFFF, (int)(255 * alpha)));

        // Category name + module count "(N)"
        String catName = category.name();
        String countStr = " (" + visible.size() + ")";
        int catNameWidth = MinecraftClient.getInstance().textRenderer.getWidth(catName);
        int countWidth = MinecraftClient.getInstance().textRenderer.getWidth(countStr);
        int totalTextW = catNameWidth + countWidth;
        int headerTextX = x + (width - totalTextW) / 2;
        cc.quark.util.RenderUtil.drawCustomText(ctx, catName, headerTextX, y + 5, 0xFFFFFFFF);
        cc.quark.util.RenderUtil.drawCustomText(ctx, countStr, headerTextX + catNameWidth, y + 5, 0xFF888888);

        // If collapsed (animated height near header), skip module rendering
        if (animatedH <= HEADER_H + 2) {
            renderBorder(ctx, alpha, HEADER_H);
            return;
        }

        // --- Modules ---
        int maxPanelHeight = MinecraftClient.getInstance().getWindow().getScaledHeight() - y - 10;
        // Clamp animated height to screen
        int displayH = Math.min(animatedH, maxPanelHeight);
        boolean needsScissor = animatedH > maxPanelHeight || animatedH < fullTotalH;

        if (needsScissor) {
            double scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
            int scissorY = (int) ((MinecraftClient.getInstance().getWindow().getScaledHeight() - (y + displayH)) * scale);
            int scissorH = (int) ((displayH - HEADER_H) * scale);
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
            } else if (!search.isEmpty() && m.getName().toLowerCase().contains(search.toLowerCase())) {
                // Search match highlight: draw name in accent color
                textColor = ClickGUI.getAccentColor();
            }

            cc.quark.util.RenderUtil.drawCustomText(ctx, m.getName(), x + 6, yy + 3, textColor);

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
                ctx.fill(x, yy, x + width, yy + settingsHeight, ColorUtil.withAlpha(0x121212, (int)(255 * alpha)));
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
        renderBorder(ctx, alpha, displayH);

        // --- Context Menu ---
        if (contextMenuModule != null) {
            int menuW = 60;
            int menuItemH = 12;
            int menuH = CONTEXT_OPTIONS.length * menuItemH + 4;
            ctx.fill(contextMenuX, contextMenuY, contextMenuX + menuW, contextMenuY + menuH,
                ColorUtil.withAlpha(0x1A1A1A, (int)(245 * alpha)));
            ctx.fill(contextMenuX, contextMenuY, contextMenuX + menuW, contextMenuY + 1,
                ColorUtil.withAlpha(ClickGUI.getAccentColor() & 0x00FFFFFF, (int)(255 * alpha)));
            int oy = contextMenuY + 2;
            for (String opt : CONTEXT_OPTIONS) {
                boolean optHovered = mx >= contextMenuX && mx <= contextMenuX + menuW
                    && my >= oy && my <= oy + menuItemH;
                if (optHovered) {
                    ctx.fill(contextMenuX, oy, contextMenuX + menuW, oy + menuItemH,
                        ColorUtil.withAlpha(0x2E2E2E, (int)(255 * alpha)));
                }
                cc.quark.util.RenderUtil.drawCustomText(ctx, opt, contextMenuX + 4, oy + 2,
                    optHovered ? 0xFFFFFFFF : 0xFFAAAAAA);
                oy += menuItemH;
            }
        }
    }

    private void renderBorder(DrawContext ctx, float alpha, int drawH) {
        int borderColor = ColorUtil.withAlpha(0x333333, (int)(255 * alpha));
        ctx.fill(x - 1, y, x, y + drawH, borderColor); // Left
        ctx.fill(x + width, y, x + width + 1, y + drawH, borderColor); // Right
        ctx.fill(x, y - 1, x + width, y, borderColor); // Top
        ctx.fill(x, y + drawH, x + width, y + drawH + 1, borderColor); // Bottom
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
        // Dismiss context menu on any click outside it
        if (contextMenuModule != null) {
            int menuW = 60;
            int menuItemH = 12;
            int menuH = CONTEXT_OPTIONS.length * menuItemH + 4;
            if (mx >= contextMenuX && mx <= contextMenuX + menuW
                    && my >= contextMenuY && my <= contextMenuY + menuH) {
                // Click inside context menu: handle option selection
                int idx = (my - contextMenuY - 2) / menuItemH;
                if (idx >= 0 && idx < CONTEXT_OPTIONS.length) {
                    if (idx == 0 && contextMenuModule != null) contextMenuModule.toggle(); // Toggle
                    // "Bind key" and "Info" are stubs for now
                }
                contextMenuModule = null;
                return true;
            }
            contextMenuModule = null;
        }

        // Header click: drag on left-click, double-click to collapse/expand
        if (mx >= x && mx <= x + width && my >= y && my <= y + HEADER_H) {
            if (button == 0) {
                long now = System.currentTimeMillis();
                if (now - lastClickTime < 350) {
                    // Double-click: toggle expanded
                    expanded = !expanded;
                    lastClickTime = 0;
                } else {
                    lastClickTime = now;
                    dragging = true;
                    dragOffX = mx - x;
                    dragOffY = my - y;
                }
                return true;
            }
        }

        // Only process module clicks when expanded (and animation mostly done)
        if (!expanded && panelHeight <= HEADER_H + 4) return false;

        // Module clicks — use cachedVisibleModules so hit-testing matches what's rendered
        List<Module> visible = (cachedVisibleModules != null ? cachedVisibleModules : modules);

        int yy = y + HEADER_H;
        for (Module m : visible) {
            if (mx >= x && mx <= x + width && my >= yy && my <= yy + MODULE_H) {
                if (button == 0) m.toggle();
                if (button == 1) {
                    // Right-click: open context menu
                    contextMenuModule = m;
                    contextMenuX = mx;
                    contextMenuY = my;
                }
                return true;
            }
            yy += MODULE_H;
            if (m == expandedModule) {
                int sy = yy + 2;
                for (Setting<?> setting : m.getSettings()) {
                    // Account for scroll offset in hit-testing
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

    /**
     * Dismisses any open right-click context menu on this panel without
     * handling any option selection.  Call this on every panel before
     * forwarding a click event so menus from other panels are closed.
     */
    public void dismissContextMenu() {
        contextMenuModule = null;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public Category getCategory() { return category; }
}
