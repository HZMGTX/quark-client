package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Direction;

public class DirectionHUD extends Module {
    private final BoolSetting showCoords = register(new BoolSetting("Coords", "Show coordinates next to compass", true));
    private final IntSetting x = register(new IntSetting("X", "HUD X position", 5, 0, 1000));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 5, 0, 600));

    public DirectionHUD() { super("DirectionHUD", "Shows compass direction and coordinates on HUD", Category.RENDER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.player == null) return;
        DrawContext ctx = e.getDrawContext();
        Direction facing = mc.player.getHorizontalFacing();
        String dir = facing.name();
        String arrow = switch (facing) {
            case NORTH -> "↑ N";
            case SOUTH -> "↓ S";
            case EAST -> "→ E";
            case WEST -> "← W";
            default -> "?";
        };
        cc.quark.util.RenderUtil.drawCustomText(ctx, arrow, x.get(), y.get(), 0xFFFFAA00);
        if (showCoords.isEnabled()) {
            String coords = String.format("%.1f, %.1f, %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
            cc.quark.util.RenderUtil.drawCustomText(ctx, coords, x.get(), y.get() + 10, 0xFFAAAAAA);
        }
    }
}
