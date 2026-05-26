package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Direction;

public class DirectionHud extends Module {

    private final IntSetting x = register(new IntSetting("X", "X pos", 4, 0, 500));
    private final IntSetting y = register(new IntSetting("Y", "Y pos", 34, 0, 500));

    public DirectionHud() {
        super("DirectionHud", "Displays the player's facing direction and yaw", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        Direction dir = mc.player.getHorizontalFacing();
        ctx.drawTextWithShadow(mc.textRenderer,
                "Facing: " + dir.getName().toUpperCase() + String.format(" (%.0f)", mc.player.getYaw()),
                x.get(), y.get(), 0xFF88CCFF);
    }
}
