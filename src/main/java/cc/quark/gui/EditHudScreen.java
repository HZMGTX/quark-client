package cc.quark.gui;

import cc.quark.Quark;
import cc.quark.module.modules.render.HUD;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class EditHudScreen extends Screen {

    private final HUD hudModule;
    private DragState dragging = DragState.NONE;
    private int dragOffX, dragOffY;

    private enum DragState {
        NONE, WATERMARK, LIST, COORDS, TARGET_HUD
    }

    public EditHudScreen() {
        super(Text.literal("Edit HUD"));
        hudModule = (HUD) Quark.getInstance().getModuleManager().getModule("HUD");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dim background
        context.fill(0, 0, width, height, 0x44000000);

        if (hudModule != null && hudModule.isEnabled()) {
            int padding = client.textRenderer.fontHeight / 2;
            
            // 1. Watermark Hitbox
            int wmX = hudModule.wmX.getValue();
            int wmY = hudModule.wmY.getValue();
            int wmWidth = client.textRenderer.getWidth("Quark.cc §7v" + Quark.VERSION) + padding * 2;
            int wmHeight = client.textRenderer.fontHeight + padding;
            drawHitbox(context, wmX, wmY, wmWidth, wmHeight, mouseX, mouseY);

            // 2. Coords Hitbox
            int coordsX = hudModule.coordsX.getValue();
            int coordsY = height - hudModule.coordsY.getValue();
            int coordsW = 120; // estimate
            int coordsH = 30;
            drawHitbox(context, coordsX, coordsY, coordsW, coordsH, mouseX, mouseY);

            // 3. List Hitbox
            int listX = width - hudModule.listX.getValue() - 80; // approximate width
            int listY = hudModule.listY.getValue();
            int listW = 80;
            int listH = 100; // approximate height
            drawHitbox(context, listX, listY, listW, listH, mouseX, mouseY);

            // 4. TargetHUD Hitbox
            cc.quark.module.modules.render.TargetHUD thud = (cc.quark.module.modules.render.TargetHUD) Quark.getInstance().getModuleManager().getModule("TargetHUD");
            if (thud != null) {
                int thX = thud.x.getValue();
                int thY = thud.y.getValue();
                drawHitbox(context, thX, thY, 140, 45, mouseX, mouseY);
            }
            
            cc.quark.util.RenderUtil.drawCustomText(context, "Drag elements to move them. Press ESC to save.", width / 2 - 80, 10, 0xFFFFFFFF);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawHitbox(DrawContext ctx, int x, int y, int w, int h, int mx, int my) {
        boolean hovered = mx >= x && mx <= x + w && my >= y && my <= y + h;
        int color = hovered ? 0x66FFFFFF : 0x33FFFFFF;
        ctx.fill(x, y, x + w, y + h, color);
        ctx.fill(x, y, x + w, y + 1, 0xFFFFFFFF); // Top border
        ctx.fill(x, y + h - 1, x + w, y + h, 0xFFFFFFFF); // Bottom
        ctx.fill(x, y, x + 1, y + h, 0xFFFFFFFF); // Left
        ctx.fill(x + w - 1, y, x + w, y + h, 0xFFFFFFFF); // Right
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hudModule == null || button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int padding = client.textRenderer.fontHeight / 2;
        
        // Watermark
        int wmX = hudModule.wmX.getValue();
        int wmY = hudModule.wmY.getValue();
        int wmWidth = client.textRenderer.getWidth("Quark.cc §7v" + Quark.VERSION) + padding * 2;
        int wmHeight = client.textRenderer.fontHeight + padding;
        if (mouseX >= wmX && mouseX <= wmX + wmWidth && mouseY >= wmY && mouseY <= wmY + wmHeight) {
            dragging = DragState.WATERMARK;
            dragOffX = (int)mouseX - wmX;
            dragOffY = (int)mouseY - wmY;
            return true;
        }

        // Coords
        int coordsX = hudModule.coordsX.getValue();
        int coordsY = height - hudModule.coordsY.getValue();
        if (mouseX >= coordsX && mouseX <= coordsX + 120 && mouseY >= coordsY && mouseY <= coordsY + 30) {
            dragging = DragState.COORDS;
            dragOffX = (int)mouseX - coordsX;
            dragOffY = (int)mouseY - coordsY;
            return true;
        }

        // List
        int listX = width - hudModule.listX.getValue() - 80;
        int listY = hudModule.listY.getValue();
        if (mouseX >= listX && mouseX <= listX + 80 && mouseY >= listY && mouseY <= listY + 100) {
            dragging = DragState.LIST;
            dragOffX = (int)mouseX - listX;
            dragOffY = (int)mouseY - listY;
            return true;
        }

        // TargetHUD
        cc.quark.module.modules.render.TargetHUD thud = (cc.quark.module.modules.render.TargetHUD) Quark.getInstance().getModuleManager().getModule("TargetHUD");
        if (thud != null) {
            int thX = thud.x.getValue();
            int thY = thud.y.getValue();
            if (mouseX >= thX && mouseX <= thX + 140 && mouseY >= thY && mouseY <= thY + 45) {
                dragging = DragState.TARGET_HUD;
                dragOffX = (int)mouseX - thX;
                dragOffY = (int)mouseY - thY;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (hudModule != null && dragging != DragState.NONE && button == 0) {
            int mx = (int)mouseX;
            int my = (int)mouseY;

            if (dragging == DragState.WATERMARK) {
                hudModule.wmX.setValue(mx - dragOffX);
                hudModule.wmY.setValue(my - dragOffY);
            } else if (dragging == DragState.COORDS) {
                hudModule.coordsX.setValue(mx - dragOffX);
                hudModule.coordsY.setValue(height - (my - dragOffY) - 30); // Save offset from bottom
            } else if (dragging == DragState.LIST) {
                // For list, we save offset from right
                int actualX = mx - dragOffX;
                hudModule.listX.setValue(width - actualX - 80);
                hudModule.listY.setValue(my - dragOffY);
            } else if (dragging == DragState.TARGET_HUD) {
                cc.quark.module.modules.render.TargetHUD thud = (cc.quark.module.modules.render.TargetHUD) Quark.getInstance().getModuleManager().getModule("TargetHUD");
                if (thud != null) {
                    thud.x.setValue(mx - dragOffX);
                    thud.y.setValue(my - dragOffY);
                }
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging != DragState.NONE && button == 0) {
            dragging = DragState.NONE;
            // Force save settings
            Quark.getInstance().getConfigManager().save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        super.close();
        Quark.getInstance().getConfigManager().save();
    }
}
