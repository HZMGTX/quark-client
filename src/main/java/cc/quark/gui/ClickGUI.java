package cc.quark.gui;

import cc.quark.Quark;
import cc.quark.gui.components.CategoryPanel;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.ModuleManager;
import cc.quark.setting.*;
import cc.quark.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends Screen {

    private final List<CategoryPanel> panels = new ArrayList<>();
    private String searchQuery = "";
    private float alpha = 0f;
    private static final int PANEL_WIDTH = 120;
    private static final int PANEL_HEADER = 16;

    public ClickGUI() {
        super(Text.literal("Quark.cc"));
    }

    @Override
    protected void init() {
        panels.clear();
        ModuleManager mm = Quark.getInstance().getModuleManager();
        int x = 10;
        for (Category cat : Category.values()) {
            panels.add(new CategoryPanel(cat, mm.getModulesForCategory(cat), x, 10, PANEL_WIDTH));
            x += PANEL_WIDTH + 5;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        alpha = Math.min(1f, alpha + delta * 0.15f);

        // Dim background
        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(),
                     ColorUtil.withAlpha(0x000000, (int)(120 * alpha)));

        // Search bar at top-right
        int sbX = context.getScaledWindowWidth() - 140;
        int sbY = 5;
        context.fill(sbX, sbY, sbX + 130, sbY + 14, ColorUtil.withAlpha(0x000000, 180));
        context.fill(sbX, sbY, sbX + 130, sbY + 1, 0xFF5555FF);
        String search = searchQuery.isEmpty() ? "Search..." : searchQuery;
        int searchColor = searchQuery.isEmpty() ? 0xFF666666 : 0xFFFFFFFF;
        context.drawTextWithShadow(client.textRenderer, search, sbX + 4, sbY + 3, searchColor);

        // Render panels
        for (CategoryPanel panel : panels) {
            panel.render(context, mouseX, mouseY, delta, searchQuery, alpha);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseScrolled((int)mouseX, (int)mouseY, verticalAmount)) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

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
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        searchQuery += chr;
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // Category accent colors
    public static int getCategoryColor(Category cat) {
        return switch (cat) {
            case COMBAT   -> 0xFFFF5555;
            case MOVEMENT -> 0xFF55FF55;
            case PLAYER   -> 0xFF5555FF;
            case RENDER   -> 0xFF55FFFF;
            case WORLD    -> 0xFFFFFF55;
            case EXPLOIT  -> 0xFFAA55FF;
        };
    }
}
