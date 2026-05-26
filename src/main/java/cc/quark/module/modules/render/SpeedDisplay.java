package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class SpeedDisplay extends Module {

    private final IntSetting x = register(new IntSetting("X", "X pos", 4, 0, 500));
    private final IntSetting y = register(new IntSetting("Y", "Y pos", 24, 0, 500));

    public SpeedDisplay() {
        super("SpeedDisplay", "Shows the player's horizontal movement speed", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        double dx = mc.player.getX() - mc.player.prevX;
        double dz = mc.player.getZ() - mc.player.prevZ;
        double bps = Math.sqrt(dx * dx + dz * dz) * 20.0;
        ctx.drawTextWithShadow(mc.textRenderer, String.format("Speed: %.2f b/s", bps), x.get(), y.get(), 0xFFFFFFFF);
    }
}
