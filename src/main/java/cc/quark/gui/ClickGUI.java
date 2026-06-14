package cc.quark.gui;

import cc.quark.Quark;
import cc.quark.gui.components.CategoryPanel;
import cc.quark.module.Category;
import cc.quark.module.ModuleManager;
import cc.quark.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends Screen {

    private static final List<CategoryPanel> panels = new ArrayList<>();
    private String searchQuery = "";
    private float alpha = 0f;
    private float currentScale = 1.0f;
    private static cc.quark.module.modules.render.ClickGuiModule cachedClickGuiModule;

    // Tab bar
    private static final int TAB_H = 22;
    private Category activeTab = Category.COMBAT; // default to first tab

    // Category display metadata
    private static final String[] CAT_ICONS = { "⚔", "🏃", "☻", "✦", "⛏", "⚡", "⚙", "🛡" };
    private static final int[] CAT_COLORS = {
        0xFFFF5555, // COMBAT
        0xFF55FF55, // MOVEMENT
        0xFFFFFF55, // PLAYER
        0xFF5599FF, // RENDER
        0xFFFF9944, // WORLD
        0xFFFF55FF, // EXPLOIT
        0xFFAAAAAA, // MISC
        0xFFFF3333, // STAFF
    };

    public static int getAccentColor() {
        if (cachedClickGuiModule == null) {
            cc.quark.module.Module mod = Quark.getInstance().getModuleManager().getModule("ClickGUI");
            if (mod instanceof cc.quark.module.modules.render.ClickGuiModule cgm) {
                cachedClickGuiModule = cgm;
            }
        }
        if (cachedClickGuiModule != null) return cachedClickGuiModule.getAccentColor();
        return ThemeManager.INSTANCE.getAccentColor();
    }

    public static int getBackgroundColor() { return ThemeManager.INSTANCE.getBackgroundColor(); }
    public static int getPanelColor()      { return ThemeManager.INSTANCE.getPanelColor(); }

    public ClickGUI() {
        super(Text.literal("Quark.cc"));
    }

    @Override
    protected void init() {
        panels.clear();
        ModuleManager mm = Quark.getInstance().getModuleManager();
        // Single-panel layout: all panels at same position, only active one shown
        // Panel width = 40% of screen, capped 160–260px
        int panelW = Math.max(160, Math.min(260, (int)(this.width * 0.25f)));
        int panelX = 10;
        int panelY = TAB_H + 4;
        for (Category cat : Category.values()) {
            panels.add(new CategoryPanel(cat, mm.getModulesForCategory(cat), panelX, panelY, panelW));
        }
    }

    public static List<CategoryPanel> getPanels() { return panels; }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Suppress default blur/dim — we draw our own
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        alpha = Math.min(1f, alpha + delta * 0.2f);

        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();

        // ── Subtle dark backdrop ──────────────────────────────────────────────
        context.fill(0, 0, screenW, screenH, ColorUtil.withAlpha(0x080808, (int)(160 * alpha)));

        currentScale = 1.0f; // no zoom — 1:1 with screen
        context.getMatrices().push();

        // ── Tab bar ──────────────────────────────────────────────────────────
        // Background strip
        context.fill(0, 0, screenW, TAB_H, ColorUtil.withAlpha(0x101010, (int)(255 * alpha)));
        // Bottom border line
        context.fill(0, TAB_H - 1, screenW, TAB_H, ColorUtil.withAlpha(0x222222, (int)(255 * alpha)));

        // Client name on left
        cc.quark.util.RenderUtil.drawCustomText(context, "QUARK", 8, 8, getAccentColor());

        // Category tabs
        Category[] cats = Category.values();
        int tabX = screenW / 2 - getTabsWidth(cats) / 2;

        for (int i = 0; i < cats.length; i++) {
            Category cat = cats[i];
            boolean active = cat == activeTab;
            int catColor = CAT_COLORS[i];
            String label  = cat.name().charAt(0) + cat.name().substring(1).toLowerCase();
            int labelW    = MinecraftClient.getInstance().textRenderer.getWidth(label);
            int tabW      = labelW + 16;

            int tabTextColor = active ? 0xFFFFFFFF : ColorUtil.withAlpha(0xFFFFFF, (int)(160 * alpha));

            if (active) {
                // Highlighted background
                context.fill(tabX, 0, tabX + tabW, TAB_H, ColorUtil.withAlpha(0x1A1A1A, (int)(255 * alpha)));
                // Bottom accent line
                context.fill(tabX, TAB_H - 2, tabX + tabW, TAB_H, ColorUtil.withAlpha(catColor & 0x00FFFFFF, (int)(255 * alpha)));
            } else {
                boolean hovered = mouseX >= tabX && mouseX <= tabX + tabW
                        && mouseY >= 0 && mouseY <= TAB_H;
                if (hovered) {
                    context.fill(tabX, 0, tabX + tabW, TAB_H, ColorUtil.withAlpha(0x151515, (int)(200 * alpha)));
                }
            }

            // Left color stripe
            context.fill(tabX, 4, tabX + 2, TAB_H - 4, ColorUtil.withAlpha(catColor & 0x00FFFFFF, (int)(220 * alpha)));

            cc.quark.util.RenderUtil.drawCustomText(context, label, tabX + 6, 8, tabTextColor);

            tabX += tabW + 4;
        }

        // Search bar (right side of tab bar)
        int sbW = 130;
        int sbX = screenW - sbW - 8;
        int sbY = 4;
        context.fill(sbX, sbY, sbX + sbW, sbY + 16, ColorUtil.withAlpha(0x1A1A1A, (int)(200 * alpha)));
        context.fill(sbX, sbY + 15, sbX + sbW, sbY + 16, ColorUtil.withAlpha(getAccentColor() & 0x00FFFFFF, (int)(200 * alpha)));
        long nowMs = System.currentTimeMillis();
        boolean showCursor = (nowMs / 500) % 2 == 0;
        String displayText = searchQuery.isEmpty() ? "Search..." : searchQuery + (showCursor ? "|" : "");
        int searchColor = searchQuery.isEmpty() ? 0xFF555555 : 0xFFFFFFFF;
        cc.quark.util.RenderUtil.drawCustomText(context, displayText, sbX + 5, sbY + 5, searchColor);

        // ── Category panels ──────────────────────────────────────────────────
        for (int i = 0; i < panels.size(); i++) {
            CategoryPanel panel = panels.get(i);
            // If a tab is active, only show that category's panel; else show all
            if (activeTab == null || cats[i] == activeTab) {
                panel.render(context, mouseX, mouseY, delta, searchQuery, alpha);
            }
        }

        // ── Watermark ────────────────────────────────────────────────────────
        String watermark = "Quark.cc | " + Quark.VERSION;
        int wmW = MinecraftClient.getInstance().textRenderer.getWidth(watermark) + 10;
        context.fill(screenW - wmW - 2, screenH - 14, screenW, screenH, ColorUtil.withAlpha(0x0A0A0A, (int)(180 * alpha)));
        cc.quark.util.RenderUtil.drawCustomText(context, watermark, screenW - wmW, screenH - 11, getAccentColor());

        context.getMatrices().pop();
    }

    private int getTabsWidth(Category[] cats) {
        int total = 0;
        for (Category cat : cats) {
            String label = cat.name().charAt(0) + cat.name().substring(1).toLowerCase();
            total += MinecraftClient.getInstance().textRenderer.getWidth(label) + 16 + 4;
        }
        return total - 4;
    }

    private double unscaleX(double mx) { return mx; }
    private double unscaleY(double my) { return my; }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double ux = unscaleX(mouseX);
        double uy = unscaleY(mouseY);

        // Tab bar click
        if (button == 0 && uy >= 0 && uy <= TAB_H) {
            Category[] cats = Category.values();
            int tabX = width / 2 - getTabsWidth(cats) / 2;
            for (int i = 0; i < cats.length; i++) {
                String label = cats[i].name().charAt(0) + cats[i].name().substring(1).toLowerCase();
                int tabW = MinecraftClient.getInstance().textRenderer.getWidth(label) + 16;
                if (ux >= tabX && ux <= tabX + tabW) {
                    activeTab = cats[i]; // always select, never deselect to null
                    return true;
                }
                tabX += tabW + 4;
            }
        }

        // Search bar clear
        if (button == 0) {
            int sbW = 130;
            int sbX = width - sbW - 8;
            if (ux >= sbX && ux <= sbX + sbW && uy >= 4 && uy <= 20) {
                if (!searchQuery.isEmpty()) {
                    // single click focuses; handled by charTyped
                }
            }
        }

        for (CategoryPanel panel : panels) panel.dismissContextMenu();

        Category[] cats = Category.values();
        for (int i = 0; i < panels.size(); i++) {
            if (activeTab == null || cats[i] == activeTab) {
                if (panels.get(i).mouseClicked((int)ux, (int)uy, button)) return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        double ux = unscaleX(mouseX);
        double uy = unscaleY(mouseY);
        double udx = deltaX / currentScale;
        double udy = deltaY / currentScale;
        for (CategoryPanel panel : panels) {
            if (panel.mouseDragged((int)ux, (int)uy, button, (int)udx, (int)udy)) return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : panels) panel.mouseReleased();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        Quark.getInstance().getConfigManager().save();
        super.close();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        double ux = unscaleX(mouseX);
        double uy = unscaleY(mouseY);
        for (CategoryPanel panel : panels) {
            if (panel.mouseScrolled((int)ux, (int)uy, vAmount)) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
    }

    private int tabFocusIndex = 0;

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { close(); return true; }
        if (keyCode == 259) {
            if (!searchQuery.isEmpty()) searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            return true;
        }
        if (keyCode == 65 && (modifiers & org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL) != 0) {
            searchQuery = ""; return true;
        }
        if (keyCode == 258) {
            if (!panels.isEmpty()) {
                boolean rev = (modifiers & org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT) != 0;
                tabFocusIndex = rev ? (tabFocusIndex - 1 + panels.size()) % panels.size()
                                    : (tabFocusIndex + 1) % panels.size();
                CategoryPanel focused = panels.get(tabFocusIndex);
                focused.setX((this.width - focused.getWidth()) / 2);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (chr >= 32 && chr <= 126) { searchQuery += chr; return true; }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }
}
