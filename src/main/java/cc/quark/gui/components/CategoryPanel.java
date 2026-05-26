package cc.quark.gui.components;

import cc.quark.gui.ClickGUI;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.*;
import cc.quark.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.*;

public class CategoryPanel {

    private final Category category;
    private final List<Module> modules;
    private int x, y;
    private final int width;
    private static final int HEADER_H  = 18;
    private static final int MODULE_H  = 14;
    private static final int SETTING_H = 14;

    private boolean dragging = false;
    private int dragOffX, dragOffY;
    private Module expandedModule = null;
    private Module lastClickedModule = null;
    private long lastModuleClickTime = 0;

    // Search caching
    private String lastSearch = null;
    private List<Module> cachedVisibleModules = null;

    // Smooth scrolling
    private float scrollOffset = 0;
    private float targetScrollOffset = 0;

    // Expand/collapse animation
    private boolean expanded = true;
    private float panelHeight = -1f;
    private long lastClickTime = 0;

    // Right-click context menu
    private Module contextMenuModule = null;
    private int contextMenuX = 0;
    private int contextMenuY = 0;
    private static final String[] CONTEXT_OPTIONS = {"Toggle", "Bind key", "Info"};

    // Favorites: pinned module names, shared across all panels
    private static final Set<String> favorites = new HashSet<>();

    // Tooltip state (Task 1)
    private Module tooltipModule = null;
    private long tooltipShowTime = 0;
    private String tooltipText = "";

    // ColorSetting cycle presets (Task 3): accent, red, green, blue, white, yellow
    private static final int[] COLOR_PRESETS = {
        0xFF00AAFF, 0xFFFF4444, 0xFF44FF88, 0xFF4488FF, 0xFFFFFFFF, 0xFFFFAA00
    };

    public CategoryPanel(Category category, List<Module> modules, int x, int y, int width) {
        this.category = category;
        this.modules  = modules;
        this.x        = x;
        this.y        = y;
        this.width    = width;
    }

    public void render(DrawContext ctx, int mx, int my, float delta, String search, float alpha) {
        // smooth scroll interpolation
        scrollOffset += (targetScrollOffset - scrollOffset) * delta * 0.4f;

        // rebuild visible module list when search changes (Task 7: fuzzy match)
        if (lastSearch == null || !lastSearch.equals(search)) {
            lastSearch = search;
            cachedVisibleModules = modules.stream()
                .filter(m -> search.isEmpty() || fuzzyMatch(m.getName(), search))
                .sorted(Comparator.<Module, Integer>comparing(m -> favorites.contains(m.getName()) ? 0 : 1)
                    .thenComparing(Module::getName))
                .toList();
        }
        List<Module> visible = cachedVisibleModules;

        int fullTotalH = HEADER_H + visible.size() * MODULE_H;
        if (expandedModule != null && visible.contains(expandedModule)) {
            fullTotalH += expandedModule.getSettings().size() * SETTING_H + 4;
        }

        float targetH = expanded ? fullTotalH : HEADER_H;
        if (panelHeight < 0) panelHeight = targetH;
        panelHeight += (targetH - panelHeight) * delta * 0.3f;
        if (Math.abs(panelHeight - targetH) < 0.5f) panelHeight = targetH;
        int animatedH = (int) panelHeight;

        // panel body
        ctx.fill(x, y, x + width, y + animatedH, ColorUtil.withAlpha(0x181818, (int)(240 * alpha)));

        // header
        ctx.fill(x, y, x + width, y + HEADER_H, ColorUtil.withAlpha(0x222222, (int)(255 * alpha)));
        ctx.fill(x, y + HEADER_H - 1, x + width, y + HEADER_H,
                 ColorUtil.withAlpha(ClickGUI.getAccentColor() & 0x00FFFFFF, (int)(255 * alpha)));

        String catName  = category.name();
        String countStr = " (" + visible.size() + ")";
        int catNameWidth = MinecraftClient.getInstance().textRenderer.getWidth(catName);
        int countWidth   = MinecraftClient.getInstance().textRenderer.getWidth(countStr);
        int totalTextW   = catNameWidth + countWidth;
        int headerTextX  = x + (width - totalTextW) / 2;
        cc.quark.util.RenderUtil.drawCustomText(ctx, catName, headerTextX, y + 5, 0xFFFFFFFF);
        cc.quark.util.RenderUtil.drawCustomText(ctx, countStr, headerTextX + catNameWidth, y + 5, 0xFF888888);

        if (animatedH <= HEADER_H + 2) {
            renderBorder(ctx, alpha, HEADER_H);
            return;
        }

        int maxPanelHeight = MinecraftClient.getInstance().getWindow().getScaledHeight() - y - 10;
        int displayH       = Math.min(animatedH, maxPanelHeight);
        boolean needsScissor = animatedH > maxPanelHeight || animatedH < fullTotalH;

        if (needsScissor) {
            double scale  = MinecraftClient.getInstance().getWindow().getScaleFactor();
            int scissorY  = (int) ((MinecraftClient.getInstance().getWindow().getScaledHeight() - (y + displayH)) * scale);
            int scissorH  = (int) ((displayH - HEADER_H) * scale);
            com.mojang.blaze3d.systems.RenderSystem.enableScissor((int)(x * scale), scissorY, (int)(width * scale), scissorH);
        }

        ctx.getMatrices().push();
        if (needsScissor) ctx.getMatrices().translate(0, -scrollOffset, 0);

        int yy = y + HEADER_H;
        // track which module the mouse is hovering (for tooltip)
        Module hoveredModule = null;

        for (Module m : visible) {
            boolean hovered = mx >= x && mx <= x + width && my >= yy && my <= yy + MODULE_H;

            if (hovered) {
                ctx.fill(x, yy, x + width, yy + MODULE_H, ColorUtil.withAlpha(0x2A2A2A, (int)(255 * alpha)));
                hoveredModule = m;
            }

            int textColor = 0xFFAAAAAA;
            if (m.isEnabled()) {
                ctx.fill(x, yy, x + 2, yy + MODULE_H,
                         ColorUtil.withAlpha(ClickGUI.getAccentColor() & 0x00FFFFFF, (int)(255 * alpha)));
                textColor = 0xFFFFFFFF;
            } else if (!search.isEmpty() && fuzzyMatch(m.getName(), search)) {
                textColor = ClickGUI.getAccentColor();
            }

            // highlight exact substring matches brighter than fuzzy-only matches
            if (!search.isEmpty() && m.getName().toLowerCase().contains(search.toLowerCase()) && !m.isEnabled()) {
                textColor = ClickGUI.getAccentColor();
            }

            if (favorites.contains(m.getName())) {
                cc.quark.util.RenderUtil.drawCustomText(ctx, "★", x + 4, yy + 3, 0xFFFFAA00);
                cc.quark.util.RenderUtil.drawCustomText(ctx, m.getName(), x + 13, yy + 3, textColor);
            } else {
                cc.quark.util.RenderUtil.drawCustomText(ctx, m.getName(), x + 6, yy + 3, textColor);
            }

            if (m.getKeybind() != 0) {
                String kb = org.lwjgl.glfw.GLFW.glfwGetKeyName(m.getKeybind(), 0);
                if (kb != null) {
                    kb = kb.toUpperCase();
                    int kbWidth = MinecraftClient.getInstance().textRenderer.getWidth(kb);
                    int badgeX  = x + width - kbWidth - 8;
                    int badgeY  = yy + 2;
                    int badgeW  = kbWidth + 6;
                    int badgeH  = MODULE_H - 4;
                    // pill background
                    ctx.fill(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH,
                             ColorUtil.withAlpha(0x222222, (int)(255 * alpha)));
                    // pill border
                    ctx.fill(badgeX,               badgeY,               badgeX + badgeW, badgeY + 1,               ColorUtil.withAlpha(0x444444, (int)(255 * alpha)));
                    ctx.fill(badgeX,               badgeY + badgeH - 1,  badgeX + badgeW, badgeY + badgeH,          ColorUtil.withAlpha(0x444444, (int)(255 * alpha)));
                    ctx.fill(badgeX,               badgeY + 1,           badgeX + 1,      badgeY + badgeH - 1,      ColorUtil.withAlpha(0x444444, (int)(255 * alpha)));
                    ctx.fill(badgeX + badgeW - 1,  badgeY + 1,           badgeX + badgeW, badgeY + badgeH - 1,      ColorUtil.withAlpha(0x444444, (int)(255 * alpha)));
                    cc.quark.util.RenderUtil.drawCustomText(ctx, kb, badgeX + 3, badgeY + 2, 0xFFFFFFFF);
                }
            }

            if (!m.getSettings().isEmpty()) {
                String arrow = (m == expandedModule) ? "v" : ">";
                cc.quark.util.RenderUtil.drawCustomText(ctx, arrow, x + width - 12, yy + 3, 0xFF666666);
            }

            yy += MODULE_H;

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
        if (needsScissor) com.mojang.blaze3d.systems.RenderSystem.disableScissor();

        renderBorder(ctx, alpha, displayH);

        // --- Context Menu ---
        if (contextMenuModule != null) {
            int menuW    = 60;
            int menuItemH = 12;
            int menuH    = CONTEXT_OPTIONS.length * menuItemH + 4;
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

        // --- Tooltip (Task 1) ---
        // update tooltip tracking
        if (hoveredModule == null) {
            tooltipModule    = null;
            tooltipShowTime  = 0;
        } else if (hoveredModule != tooltipModule) {
            tooltipModule    = hoveredModule;
            tooltipShowTime  = System.currentTimeMillis();
            tooltipText      = hoveredModule.getDescription();
        }

        if (tooltipModule != null && !tooltipText.isEmpty()
                && System.currentTimeMillis() - tooltipShowTime > 300) {
            int ttW  = MinecraftClient.getInstance().textRenderer.getWidth(tooltipText) + 8;
            int ttH  = 14;
            int ttX  = Math.min(mx + 6, MinecraftClient.getInstance().getWindow().getScaledWidth() - ttW - 2);
            int ttY  = my + 14;
            // keep within screen height
            int screenH2 = MinecraftClient.getInstance().getWindow().getScaledHeight();
            if (ttY + ttH > screenH2 - 2) ttY = my - ttH - 2;

            ctx.fill(ttX - 1, ttY - 1, ttX + ttW + 1, ttY + ttH + 1, ColorUtil.withAlpha(0x000000, 200));
            ctx.fill(ttX, ttY, ttX + ttW, ttY + ttH, ColorUtil.withAlpha(0x1A1A1A, 230));
            ctx.fill(ttX, ttY, ttX + ttW, ttY + 1, ColorUtil.withAlpha(ClickGUI.getAccentColor() & 0x00FFFFFF, 200));
            cc.quark.util.RenderUtil.drawCustomText(ctx, tooltipText, ttX + 4, ttY + 3, 0xFFCCCCCC);
        }
    }

    /** Standard fuzzy match: all chars of query appear in order in name (case-insensitive). */
    private static boolean fuzzyMatch(String name, String query) {
        if (query.isEmpty()) return true;
        String n = name.toLowerCase();
        String q = query.toLowerCase();
        int ni = 0, qi = 0;
        while (ni < n.length() && qi < q.length()) {
            if (n.charAt(ni) == q.charAt(qi)) qi++;
            ni++;
        }
        return qi == q.length();
    }

    private void renderBorder(DrawContext ctx, float alpha, int drawH) {
        int borderColor = ColorUtil.withAlpha(0x333333, (int)(255 * alpha));
        ctx.fill(x - 1, y,          x,          y + drawH,     borderColor); // Left
        ctx.fill(x + width, y,      x + width + 1, y + drawH,  borderColor); // Right
        ctx.fill(x, y - 1,          x + width,  y,             borderColor); // Top
        ctx.fill(x, y + drawH,      x + width,  y + drawH + 1, borderColor); // Bottom
    }

    private void renderSetting(DrawContext ctx, Setting<?> setting, int sx, int sy, int sw, int mx, int my) {
        int labelColor = 0xFFCCCCCC;
        cc.quark.util.RenderUtil.drawCustomText(ctx, setting.getName(), sx + 2, sy + 3, labelColor);

        if (setting instanceof BoolSetting bs) {
            int toggleW  = 20;
            int toggleH  = 10;
            int toggleX  = sx + sw - toggleW - 2;
            int toggleY  = sy + 2;
            boolean val  = bs.getValue();
            ctx.fill(toggleX - 1, toggleY - 1, toggleX + toggleW + 1, toggleY + toggleH + 1, 0xFF000000);
            ctx.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH,
                     val ? ClickGUI.getAccentColor() : 0xFF333333);
            int knobX = val ? toggleX + toggleW - 8 : toggleX + 1;
            ctx.fill(knobX, toggleY + 1, knobX + 7, toggleY + toggleH - 1, 0xFFFFFFFF);

        } else if (setting instanceof DoubleSetting ds) {
            int barW  = sw / 2 - 4;
            int barX  = sx + sw - barW - 2;
            int barY  = sy + 5;
            int barH  = 4;
            double pct = (ds.getValue() - ds.getMin()) / (ds.getMax() - ds.getMin());
            ctx.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF000000);
            ctx.fill(barX, barY, barX + barW, barY + barH, 0xFF333333);
            ctx.fill(barX, barY, barX + (int)(barW * pct), barY + barH, ClickGUI.getAccentColor());
            String valStr = String.format("%.2f", ds.getValue());
            int vw = MinecraftClient.getInstance().textRenderer.getWidth(valStr);
            cc.quark.util.RenderUtil.drawCustomText(ctx, valStr, barX - vw - 4, sy + 3, 0xFFAAAAAA);

        } else if (setting instanceof IntSetting is) {
            int barW  = sw / 2 - 4;
            int barX  = sx + sw - barW - 2;
            int barY  = sy + 5;
            int barH  = 4;
            double pct = (double)(is.getValue() - is.getMin()) / (is.getMax() - is.getMin());
            ctx.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF000000);
            ctx.fill(barX, barY, barX + barW, barY + barH, 0xFF333333);
            ctx.fill(barX, barY, barX + (int)(barW * pct), barY + barH, ClickGUI.getAccentColor());
            String valStr = String.valueOf(is.getValue());
            int vw = MinecraftClient.getInstance().textRenderer.getWidth(valStr);
            cc.quark.util.RenderUtil.drawCustomText(ctx, valStr, barX - vw - 4, sy + 3, 0xFFAAAAAA);

        } else if (setting instanceof ModeSetting ms) {
            String val = ms.getValue();
            int valW   = MinecraftClient.getInstance().textRenderer.getWidth(val);
            int valX   = sx + sw - valW - 8;
            cc.quark.util.RenderUtil.drawCustomText(ctx, "<", valX - 8, sy + 3, 0xFF777777);
            cc.quark.util.RenderUtil.drawCustomText(ctx, val, valX, sy + 3, 0xFFEEEEEE);
            cc.quark.util.RenderUtil.drawCustomText(ctx, ">", valX + valW + 2, sy + 3, 0xFF777777);

        } else if (setting instanceof EnumSetting<?> es) {
            // Task 2: EnumSetting rendering
            String val = es.get().name();
            int valW   = MinecraftClient.getInstance().textRenderer.getWidth(val);
            int valX   = sx + sw - valW - 8;
            cc.quark.util.RenderUtil.drawCustomText(ctx, "<", valX - 8, sy + 3, 0xFF777777);
            cc.quark.util.RenderUtil.drawCustomText(ctx, val, valX, sy + 3, 0xFFEEEEEE);
            cc.quark.util.RenderUtil.drawCustomText(ctx, ">", valX + valW + 2, sy + 3, 0xFF777777);

        } else if (setting instanceof ColorSetting cs) {
            // Task 3: ColorSetting rendering — show colored square on right
            int sqSize = 10;
            int sqX    = sx + sw - sqSize - 2;
            int sqY    = sy + 2;
            int color  = cs.get();
            // black border around square
            ctx.fill(sqX - 1, sqY - 1, sqX + sqSize + 1, sqY + sqSize + 1, 0xFF000000);
            ctx.fill(sqX, sqY, sqX + sqSize, sqY + sqSize, color);
        }
    }

    public boolean mouseClicked(int mx, int my, int button) {
        if (contextMenuModule != null) {
            int menuW    = 60;
            int menuItemH = 12;
            int menuH    = CONTEXT_OPTIONS.length * menuItemH + 4;
            if (mx >= contextMenuX && mx <= contextMenuX + menuW
                    && my >= contextMenuY && my <= contextMenuY + menuH) {
                int idx = (my - contextMenuY - 2) / menuItemH;
                if (idx >= 0 && idx < CONTEXT_OPTIONS.length) {
                    if (idx == 0 && contextMenuModule != null) contextMenuModule.toggle();
                }
                contextMenuModule = null;
                return true;
            }
            contextMenuModule = null;
        }

        if (mx >= x && mx <= x + width && my >= y && my <= y + HEADER_H) {
            if (button == 0) {
                long now = System.currentTimeMillis();
                if (now - lastClickTime < 350) {
                    expanded = !expanded;
                    lastClickTime = 0;
                } else {
                    lastClickTime = now;
                    dragging  = true;
                    dragOffX  = mx - x;
                    dragOffY  = my - y;
                }
                return true;
            }
        }

        if (!expanded && panelHeight <= HEADER_H + 4) return false;

        List<Module> visible = (cachedVisibleModules != null ? cachedVisibleModules : modules);

        int yy = y + HEADER_H;
        for (Module m : visible) {
            if (mx >= x && mx <= x + width && my >= yy && my <= yy + MODULE_H) {
                if (button == 0) {
                    long now = System.currentTimeMillis();
                    boolean isDoubleClick = (m == lastClickedModule) && (now - lastModuleClickTime < 350);
                    lastClickedModule   = m;
                    lastModuleClickTime = now;
                    if (isDoubleClick) {
                        // double-click: toggle favorite
                        if (favorites.contains(m.getName())) {
                            favorites.remove(m.getName());
                        } else {
                            favorites.add(m.getName());
                        }
                        lastSearch = null; // force re-sort
                    } else if (!m.getSettings().isEmpty() && mx >= x + width - 14) {
                        expandedModule = (m == expandedModule) ? null : m;
                    } else {
                        m.toggle();
                    }
                }
                if (button == 1) {
                    contextMenuModule = m;
                    contextMenuX      = mx;
                    contextMenuY      = my;
                }
                return true;
            }
            yy += MODULE_H;
            if (m == expandedModule) {
                int sy = yy + 2;
                for (Setting<?> setting : m.getSettings()) {
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
            if (relX >= w - 24) bs.setValue(!bs.getValue());
        } else if (setting instanceof ModeSetting ms) {
            if (button == 0) ms.next();
            if (button == 1) ms.previous();
        } else if (setting instanceof EnumSetting<?> es) {
            // Task 2: left click cycles forward, right click cycles backward
            if (button == 0) es.cycle();
            if (button == 1) {
                // cycle backwards: call cycle() (n-1) times since EnumSetting only has cycle()
                int len = es.getValues().length;
                for (int i = 0; i < len - 1; i++) es.cycle();
            }
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
        } else if (setting instanceof ColorSetting cs) {
            // Task 3: click on color square cycles through presets
            int sqX = w - 10 - 2;
            if (relX >= sqX - 2) {
                int cur = cs.get();
                // find current preset index
                int idx = -1;
                for (int i = 0; i < COLOR_PRESETS.length; i++) {
                    if (COLOR_PRESETS[i] == cur) { idx = i; break; }
                }
                // cycle to next preset
                int next = COLOR_PRESETS[(idx + 1) % COLOR_PRESETS.length];
                cs.setValue(next);
            }
        }
    }

    public boolean mouseDragged(int mx, int my, int button, int dx, int dy) {
        if (dragging && button == 0) {
            x = mx - dragOffX;
            y = my - dragOffY;
            return true;
        }
        if (expandedModule != null) {
            int yy = y + HEADER_H;
            for (Module m : modules) {
                yy += MODULE_H;
                if (m == expandedModule) {
                    int sy = yy + 2;
                    for (Setting<?> setting : m.getSettings()) {
                        if (my >= sy - 2 && my <= sy + SETTING_H + 2) {
                            if (button == 0) {
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
            float maxScroll = Math.max(0, totalH - maxPanelHeight);
            targetScrollOffset = Math.max(0, Math.min(targetScrollOffset, maxScroll));
            return true;
        }
        return false;
    }

    public void dismissContextMenu() {
        contextMenuModule = null;
    }

    public List<Module> getVisibleModules(String search) {
        if (search == null || search.isEmpty()) return modules;
        return modules.stream()
            .filter(m -> fuzzyMatch(m.getName(), search))
            .toList();
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public Category getCategory() { return category; }
}
