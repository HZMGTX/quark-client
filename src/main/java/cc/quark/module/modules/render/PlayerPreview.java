package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

/**
 * PlayerPreview — renders a 3D player model preview in a corner of the HUD,
 * similar to the inventory screen player model but overlaid on the game world.
 */
public class PlayerPreview extends Module {

    private final ModeSetting corner = register(new ModeSetting(
            "Corner", "Screen corner to display in",
            "Bottom Left", "Bottom Left", "Bottom Right", "Top Left", "Top Right"));
    private final IntSetting size = register(new IntSetting(
            "Size", "Player model render size", 40, 10, 100));
    private final IntSetting xPad = register(new IntSetting(
            "X Padding", "Horizontal padding from edge", 10, 0, 100));
    private final IntSetting yPad = register(new IntSetting(
            "Y Padding", "Vertical padding from edge", 10, 0, 100));
    private final BoolSetting background = register(new BoolSetting(
            "Background", "Draw a dark background panel", true));
    private final BoolSetting followMouse = register(new BoolSetting(
            "Follow Mouse", "Model head tracks mouse cursor", true));

    public PlayerPreview() {
        super("PlayerPreview", "Shows a 3D player model preview in the corner of the HUD", Category.RENDER);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int s = size.get();
        int padX = xPad.get();
        int padY = yPad.get();

        // Panel dimensions: slightly larger than the model size
        int panelW = s + 10;
        int panelH = (int) (s * 2.4f) + 10;

        // Compute anchor position
        int panelX, panelY;
        switch (corner.get()) {
            case "Bottom Right" -> { panelX = sw - panelW - padX; panelY = sh - panelH - padY; }
            case "Top Left"     -> { panelX = padX;                panelY = padY; }
            case "Top Right"    -> { panelX = sw - panelW - padX; panelY = padY; }
            default             -> { panelX = padX;                panelY = sh - panelH - padY; } // Bottom Left
        }

        // Draw background panel
        if (background.isEnabled()) {
            ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0x88000000);
            // Border
            ctx.fill(panelX,              panelY,               panelX + panelW, panelY + 1,       0xFF333333);
            ctx.fill(panelX,              panelY + panelH - 1,  panelX + panelW, panelY + panelH,  0xFF333333);
            ctx.fill(panelX,              panelY,               panelX + 1,      panelY + panelH,  0xFF333333);
            ctx.fill(panelX + panelW - 1, panelY,               panelX + panelW, panelY + panelH,  0xFF333333);
        }

        // Center of model render
        int modelX = panelX + panelW / 2;
        int modelY = panelY + panelH - 8;

        // Head rotation: track mouse if enabled, otherwise use player's yaw
        float mouseX, mouseY;
        if (followMouse.isEnabled()) {
            mouseX = (float) (mc.mouse.getX() * sw / mc.getWindow().getWidth());
            mouseY = (float) (mc.mouse.getY() * sh / mc.getWindow().getHeight());
        } else {
            // Fixed forward-facing angles based on player rotation
            mouseX = modelX - (mc.player.getYaw() / 180f) * s;
            mouseY = modelY - (int) (s * 1.2f) - (mc.player.getPitch() / 90f) * s;
        }

        InventoryScreen.drawEntity(ctx, panelX, panelY, panelX + panelW, panelY + panelH, s, 0.0625f, mouseX, mouseY, mc.player);
    }
}
