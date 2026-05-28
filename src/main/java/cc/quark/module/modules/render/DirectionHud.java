package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class DirectionHud extends Module {

    private final IntSetting    posX      = register(new IntSetting("X", "HUD X position", 0, 0, 3000));
    private final IntSetting    posY      = register(new IntSetting("Y", "HUD Y position", 2, 0, 3000));
    private final ColorSetting  highlight = register(new ColorSetting("Highlight", "Facing direction highlight color", 0xFF55FFFF));

    private static final String[] DIRS   = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    private static final int     BAR_W   = 160;
    private static final int     TICK_H  = 6;

    public DirectionHud() {
        super("DirectionHud", "Compass bar at the top of the screen showing cardinal directions", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.getWindow() == null) return;
        DrawContext ctx = event.getDrawContext();

        int sw = mc.getWindow().getScaledWidth();
        int cx = posX.get() == 0 ? sw / 2 : posX.get();
        int cy = posY.get();
        int lh = mc.textRenderer.fontHeight;

        float yaw = ((mc.player.getYaw() % 360f) + 360f) % 360f;

        ctx.fill(cx - BAR_W / 2 - 2, cy - 1, cx + BAR_W / 2 + 2, cy + lh + TICK_H + 3, 0xAA111111);

        for (int i = -180; i <= 180; i += 5) {
            float angle = ((yaw + i) % 360f + 360f) % 360f;
            int screenX = cx + i * BAR_W / 360;
            if (screenX < cx - BAR_W / 2 || screenX > cx + BAR_W / 2) continue;

            int dirIdx = (int)(angle / 45f + 0.5f) % 8;
            boolean isCardinal = ((int)(angle / 45f + 0.5f)) % 8 == dirIdx
                    && (i % 45 == 0 || Math.abs(i % 45) < 3);

            if (i % 45 == 0 && Math.abs(((yaw + i) % 45 + 45) % 45 - 0) < 2.5f) {
                String dir = DIRS[dirIdx];
                boolean facing = Math.abs(i) < 23;
                int col = facing ? highlight.get() : 0xFFCCCCCC;
                int tw = mc.textRenderer.getWidth(dir);
                ctx.drawTextWithShadow(mc.textRenderer, dir, screenX - tw / 2, cy, col);
                ctx.fill(screenX, cy + lh + 1, screenX + 1, cy + lh + TICK_H, facing ? highlight.get() : 0xAA888888);
            } else if (i % 15 == 0) {
                ctx.fill(screenX, cy + lh + 2, screenX + 1, cy + lh + TICK_H - 1, 0x88555555);
            }
        }

        ctx.fill(cx, cy, cx + 1, cy + lh + TICK_H + 2, highlight.get());

        String yawStr = String.format("%.1f", yaw);
        int yw = mc.textRenderer.getWidth(yawStr);
        ctx.drawTextWithShadow(mc.textRenderer, yawStr, cx - yw / 2, cy + lh + TICK_H + 3, 0xFFAAAAAA);
    }
}
