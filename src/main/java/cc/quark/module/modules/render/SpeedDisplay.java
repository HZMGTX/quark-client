package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class SpeedDisplay extends Module {

    private final IntSetting  posX     = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting  posY     = register(new IntSetting("Y", "HUD Y position", 24, 0, 3000));
    private final BoolSetting showMax  = register(new BoolSetting("Show Max", "Show recorded max speed", true));
    private final BoolSetting showVert = register(new BoolSetting("Vertical", "Also show vertical speed", false));

    private double prevX, prevY, prevZ;
    private double bps;
    private double vertBps;
    private double maxBps;
    private boolean firstTick = true;

    public SpeedDisplay() {
        super("SpeedDisplay", "Shows movement speed in blocks per second with max record tracking", Category.RENDER);
    }

    @Override
    public void onEnable() {
        firstTick = true;
        maxBps = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (firstTick) {
            prevX = mc.player.getX();
            prevY = mc.player.getY();
            prevZ = mc.player.getZ();
            firstTick = false;
            return;
        }
        double dx = mc.player.getX() - prevX;
        double dy = mc.player.getY() - prevY;
        double dz = mc.player.getZ() - prevZ;
        bps = Math.sqrt(dx * dx + dz * dz) * 20.0;
        vertBps = Math.abs(dy) * 20.0;
        if (bps > maxBps) maxBps = bps;
        prevX = mc.player.getX();
        prevY = mc.player.getY();
        prevZ = mc.player.getZ();
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int x = posX.get(), y = posY.get();
        int lh = mc.textRenderer.fontHeight + 2;

        int speedColor = bps >= 8.0 ? 0xFF55FF55 : bps >= 4.0 ? 0xFFFFFF55 : 0xFFFF5555;
        ctx.drawTextWithShadow(mc.textRenderer, String.format("Speed: %.2f b/s", bps), x, y, speedColor);

        if (showVert.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, String.format("Vert: %.2f b/s", vertBps), x, y + lh, 0xFFAAAAAA);
        }

        if (showMax.isEnabled()) {
            int dy2 = showVert.isEnabled() ? y + lh * 2 : y + lh;
            ctx.drawTextWithShadow(mc.textRenderer, String.format("Max: %.2f b/s", maxBps), x, dy2, 0xFF88CCFF);
        }
    }
}
