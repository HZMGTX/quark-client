package cc.quark.gui;

import cc.quark.Quark;
import cc.quark.gui.components.CategoryPanel;
import cc.quark.module.Category;
import cc.quark.module.ModuleManager;
import cc.quark.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends Screen {

    private static final List<CategoryPanel> panels = new ArrayList<>();
    private String searchQuery = "";
    private float alpha = 0f;
    private static final int PANEL_WIDTH = 130;
    // Global accent color fetched from ClickGuiModule
    public static int getAccentColor() {
        cc.quark.module.Module mod = Quark.getInstance().getModuleManager().getModule("ClickGUI");
        if (mod instanceof cc.quark.module.modules.render.ClickGuiModule cgm) {
            return cgm.getAccentColor();
        }
        return 0xFF00AAFF; // Fallback
    }

    public ClickGUI() {
        super(Text.literal("Quark.cc"));
    }

    @Override
    protected void init() {
        if (!panels.isEmpty()) return; // Already initialized, keep positions

        ModuleManager mm = Quark.getInstance().getModuleManager();
        
        // Center panels roughly on screen
        int totalWidth = Category.values().length * (PANEL_WIDTH + 10) - 10;
        int startX = Math.max(10, (this.width - totalWidth) / 2);
        
        int x = startX;
        for (Category cat : Category.values()) {
            panels.add(new CategoryPanel(cat, mm.getModulesForCategory(cat), x, 50, PANEL_WIDTH));
            x += PANEL_WIDTH + 10;
        }
    }

    public static List<CategoryPanel> getPanels() {
        return panels;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        alpha = Math.min(1f, alpha + delta * 0.15f);

        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();

        // Dark dim backdrop
        context.fill(0, 0, screenW, screenH, ColorUtil.withAlpha(0x050505, (int)(180 * alpha)));

        // --- Scale-In Animation Matrix ---
        float ease = alpha == 1.0f ? 1.0f : 1.0f - (float)Math.pow(2, -10 * alpha);
        float animScale = 0.8f + (0.2f * ease);

        context.getMatrices().push();
        
        // Pivot around the center of the screen
        int centerX = screenW / 2;
        int centerY = screenH / 2;
        context.getMatrices().translate(centerX, centerY, 0);
        context.getMatrices().scale(animScale, animScale, 1.0f);
        // Translate back
        context.getMatrices().translate(-centerX, -centerY, 0);

        // Modern flat search bar
        int sbWidth = 200;
        int sbX = centerX - (sbWidth / 2);
        int sbY = 20; // Above the panels
        
        context.fill(sbX, sbY, sbX + sbWidth, sbY + 18, ColorUtil.withAlpha(0x101010, 255));
        context.fill(sbX, sbY + 17, sbX + sbWidth, sbY + 18, getAccentColor()); // Accent underline
        
        String search = searchQuery.isEmpty() ? "Search modules..." : searchQuery;
        int searchColor = searchQuery.isEmpty() ? 0xFF888888 : 0xFFFFFFFF;
        cc.quark.util.RenderUtil.drawCustomText(context, search, sbX + 6, sbY + 5, searchColor);

        // Render category panels
        for (CategoryPanel panel : panels) {
            panel.render(context, mouseX, mouseY, delta, searchQuery, alpha);
        }

        // --- Watermark: "Quark.cc" in accent color, bottom-right ---
        String watermark = "Quark.cc";
        int wmX = screenW - 60;
        int wmY = screenH - 20;
        context.fill(wmX - 4, wmY - 3, wmX + 52, wmY + 11, ColorUtil.withAlpha(0x0A0A0A, (int)(200 * alpha)));
        cc.quark.util.RenderUtil.drawCustomText(context, watermark, wmX, wmY, getAccentColor());

        context.getMatrices().pop();

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Dismiss any open context menus on ALL panels before processing the click.
        // This ensures right-click menus from other panels are closed whenever the
        // user clicks anywhere on screen, not just within the panel that owns the menu.
        for (CategoryPanel panel : panels) {
            panel.dismissContextMenu();
        }

        for (CategoryPanel panel : panels) {
            if (panel.mouseClicked((int)mouseX, (int)mouseY, button)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseDragged((int)mouseX, (int)mouseY, button, (int)deltaX, (int)deltaY)) return true;
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseScrolled((int)mouseX, (int)mouseY, verticalAmount)) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    // Index of the currently "focused" panel for TAB cycling
    private int tabFocusIndex = 0;

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            close();
            return true;
        }
        if (keyCode == 259) { // Backspace
            if (!searchQuery.isEmpty()) searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            return true;
        }
        // CTRL+A: clear search
        if (keyCode == 65 && (modifiers & org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL) != 0) {
            searchQuery = "";
            return true;
        }
        // TAB: cycle focus to next panel (shift its position to screen center-ish)
        if (keyCode == 258) { // GLFW_KEY_TAB
            if (!panels.isEmpty()) {
                boolean reverse = (modifiers & org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT) != 0;
                if (reverse) {
                    tabFocusIndex = (tabFocusIndex - 1 + panels.size()) % panels.size();
                } else {
                    tabFocusIndex = (tabFocusIndex + 1) % panels.size();
                }
                // Scroll the focused panel into a visible horizontal position
                CategoryPanel focused = panels.get(tabFocusIndex);
                int targetX = (this.width - PANEL_WIDTH) / 2;
                focused.setX(targetX);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        // Only accept printable characters
        if (chr >= 32 && chr <= 126) {
            searchQuery += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
