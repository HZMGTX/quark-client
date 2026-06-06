package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class DirectionHUD extends Module {

    private final BoolSetting compact = register(new BoolSetting("Compact", "Show only arrow + cardinal (no degrees)", false));
    private final IntSetting x = register(new IntSetting("X", "HUD X position", 5, 0, 3840));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 5, 0, 2160));

    public DirectionHUD() {
        super("DirectionHUD", "Shows cardinal direction and heading degrees on the HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        float yaw = MathHelper.wrapDegrees(mc.player.getYaw());
        if (yaw < 0) yaw += 360f;

        Direction facing = mc.player.getHorizontalFacing();
        String arrow = switch (facing) {
            case NORTH -> "N";
            case SOUTH -> "S";
            case EAST  -> "E";
            case WEST  -> "W";
            default    -> "?";
        };

        int px = x.get(), py = y.get();

        if (compact.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, arrow, px, py, 0xFFFFAA00);
        } else {
            String heading = String.format("%s  %.1f°", arrow, yaw);
            ctx.drawTextWithShadow(mc.textRenderer, heading, px, py, 0xFFFFAA00);
        }
    }
}
