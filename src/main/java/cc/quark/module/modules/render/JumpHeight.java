package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class JumpHeight extends Module {

    private final IntSetting x = register(new IntSetting("X", "HUD X position", 4,   0, 1920));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 120, 0, 1080));

    private double jumpStartY   = 0.0;
    private double maxHeightSoFar = 0.0;
    private double lastMaxHeight  = 0.0;
    private boolean wasOnGround   = true;

    public JumpHeight() {
        super("JumpHeight", "Shows current jump height tracker", Category.RENDER);
    }

    @Override
    public void onEnable() {
        jumpStartY     = 0.0;
        maxHeightSoFar = 0.0;
        lastMaxHeight  = 0.0;
        wasOnGround    = true;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();
        double  currentY = mc.player.getY();

        if (!wasOnGround && onGround) {
            // Just landed — record the max
            lastMaxHeight  = maxHeightSoFar;
            maxHeightSoFar = 0.0;
        } else if (wasOnGround && !onGround) {
            // Just left the ground — start tracking
            jumpStartY     = currentY;
            maxHeightSoFar = 0.0;
        } else if (!onGround) {
            double heightAboveStart = currentY - jumpStartY;
            if (heightAboveStart > maxHeightSoFar) {
                maxHeightSoFar = heightAboveStart;
            }
        }

        wasOnGround = onGround;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        double display = mc.player.isOnGround() ? lastMaxHeight : maxHeightSoFar;

        String label = String.format("Jump: %.2f b", display);
        ctx.drawTextWithShadow(mc.textRenderer, label, x.get(), y.get(), 0xFFFFFFFF);
    }
}
