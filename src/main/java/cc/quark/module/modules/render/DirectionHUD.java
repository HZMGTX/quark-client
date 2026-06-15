package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class DirectionHud extends Module {

    private final IntSetting   posX      = register(new IntSetting("X", "HUD X position (0=center)", 0, 0, 3000));
    private final IntSetting   posY      = register(new IntSetting("Y", "HUD Y position", 2, 0, 3000));
    private final ColorSetting highlight = register(new ColorSetting("Highlight", "Facing direction highlight color", 0xFF55FFFF));

    private static final int BAR_W = 180;
    private static final int TICK_H = 6;

    private static final String[] CARDINALS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
    private static final float[]  CARD_DEGS = {0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f};

    public DirectionHud() {
        super("DirectionHud", "Compass bar showing N/S/E/W with the player's facing direction highlighted", Category.RENDER);
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

        int left  = cx - BAR_W / 2;
        int right = cx + BAR_W / 2;
        int top   = cy;
        int bot   = cy + lh + TICK_H + 2;

        ctx.fill(left - 2, top - 1, right + 2, bot + 1, 0xAA111111);

        for (int i = 0; i < 8; i++) {
            float cardYaw = CARD_DEGS[i];
            float delta = cardYaw - yaw;
            if (delta > 180f)  delta -= 360f;
            if (delta < -180f) delta += 360f;

            if (Math.abs(delta) > 90f) continue;

            int screenX = cx + (int)(delta * BAR_W / 180f);
            if (screenX < left || screenX > right) continue;

            boolean facing = Math.abs(delta) < 15f;
            int col = facing ? highlight.get() : 0xFFCCCCCC;
            String label = CARDINALS[i];
            int tw = mc.textRenderer.getWidth(label);
            ctx.drawTextWithShadow(mc.textRenderer, label, screenX - tw / 2, cy, col);
            ctx.fill(screenX, cy + lh + 1, screenX + 1, cy + lh + TICK_H, facing ? highlight.get() : 0xAA888888);
        }

        for (int deg = -90; deg <= 90; deg += 10) {
            if (deg % 45 == 0) continue;
            int screenX = cx + deg * BAR_W / 180;
            if (screenX < left || screenX > right) continue;
            ctx.fill(screenX, cy + lh + 2, screenX + 1, cy + lh + TICK_H - 1, 0x88555555);
        }

        ctx.fill(cx, cy - 1, cx + 1, bot, highlight.get());

        String yawStr = String.format("%.1f°", yaw);
        int yw = mc.textRenderer.getWidth(yawStr);
        ctx.drawTextWithShadow(mc.textRenderer, yawStr, cx - yw / 2, bot + 1, 0xFFAAAAAA);
    }
}
