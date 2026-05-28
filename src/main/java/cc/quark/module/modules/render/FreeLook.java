package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public class FreeLook extends Module {

    private final BoolSetting lockBody = register(new BoolSetting(
            "Lock Body", "Freeze packet rotation while looking freely (hold Alt)", true));
    private final BoolSetting hudIndicator = register(new BoolSetting(
            "HUD Indicator", "Show indicator when free look is active", true));

    private float lockedYaw;
    private float lockedPitch;
    private boolean active;

    public FreeLook() {
        super("FreeLook", "Hold Alt to look around freely without rotating body or sending real rotation to server", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            lockedYaw   = mc.player.getYaw();
            lockedPitch = mc.player.getPitch();
        }
        active = false;
    }

    @Override
    public void onDisable() {
        active = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        boolean altHeld = GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS
                       || GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
        if (altHeld && !active) {
            lockedYaw   = mc.player.getYaw();
            lockedPitch = mc.player.getPitch();
            active = true;
        } else if (!altHeld) {
            active = false;
        }
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;
        if (active && lockBody.isEnabled()) {
            event.setYaw(lockedYaw);
            event.setPitch(lockedPitch);
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!hudIndicator.isEnabled() || !active) return;
        DrawContext ctx = event.getDrawContext();
        if (mc.getWindow() == null) return;
        int sw = mc.getWindow().getScaledWidth();
        String text = "FREE LOOK";
        int tw = mc.textRenderer.getWidth(text);
        int x = sw / 2 - tw / 2;
        int y = mc.getWindow().getScaledHeight() - 30;
        ctx.fill(x - 3, y - 2, x + tw + 3, y + mc.textRenderer.fontHeight + 2, 0xAA111111);
        ctx.drawTextWithShadow(mc.textRenderer, text, x, y, 0xFF55FFFF);
    }

    public float getSavedYaw()   { return lockedYaw; }
    public float getSavedPitch() { return lockedPitch; }
    public boolean isActive()    { return active; }
}
