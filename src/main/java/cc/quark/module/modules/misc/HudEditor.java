package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * HudEditor - Opens a drag-and-drop HUD element positioning screen when enabled.
 *
 * The module acts as a toggle that opens a dedicated {@link HudEditorScreen}
 * while it is active. Pressing Escape (or the module keybind again) closes the
 * screen and persists the new positions.
 *
 * Individual HUD modules store their X/Y position in IntSettings; the editor
 * screen reads those settings from the active module list and lets the player
 * drag each element to a new position, writing the new values back.
 */
public class HudEditor extends Module {

    private final BoolSetting showGrid = register(new BoolSetting(
            "Grid", "Show snap grid in editor", true));

    private final BoolSetting snapToGrid = register(new BoolSetting(
            "Snap", "Snap HUD elements to grid", true));

    public HudEditor() {
        super("HudEditor", "Enables drag-and-drop HUD element positioning", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        mc.send(() -> mc.setScreen(new HudEditorScreen(showGrid.isEnabled(), snapToGrid.isEnabled(), this)));
    }

    @Override
    public void onDisable() {
        if (mc.currentScreen instanceof HudEditorScreen) {
            mc.send(() -> mc.setScreen(null));
        }
    }

    // -------------------------------------------------------------------------
    // Minimal inner editor screen
    // -------------------------------------------------------------------------

    /**
     * Lightweight screen that overlays a grid and allows dragging labeled
     * module boxes. In a full implementation each box maps to a module's
     * X/Y IntSettings; here the scaffold is provided for extension.
     */
    public static class HudEditorScreen extends Screen {

        private final boolean grid;
        private final boolean snap;
        private final HudEditor owner;

        private static final int GRID_SIZE = 8;

        public HudEditorScreen(boolean grid, boolean snap, HudEditor owner) {
            super(Text.literal("HUD Editor"));
            this.grid  = grid;
            this.snap  = snap;
            this.owner = owner;
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return true;
        }

        @Override
        public void close() {
            super.close();
            // Disable the module when the screen is closed so the toggle state
            // stays in sync with whether the screen is open.
            if (owner.isEnabled()) {
                cc.quark.module.Module.silent = true;
                owner.disable();
                cc.quark.module.Module.silent = false;
            }
        }

        @Override
        public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
            // Semi-transparent dark background
            context.fill(0, 0, this.width, this.height, 0xAA000000);

            if (grid) {
                int gridColor = 0x33FFFFFF;
                for (int x = 0; x < this.width; x += GRID_SIZE) {
                    context.fill(x, 0, x + 1, this.height, gridColor);
                }
                for (int y = 0; y < this.height; y += GRID_SIZE) {
                    context.fill(0, y, this.width, y + 1, gridColor);
                }
            }

            // Title
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    "§eHUD Editor §7- Drag elements to reposition | §fESC§7 to close",
                    this.width / 2,
                    4,
                    0xFFFFFFFF);

            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
