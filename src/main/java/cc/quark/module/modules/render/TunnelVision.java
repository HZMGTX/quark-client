package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;

public class TunnelVision extends Module {
    private final IntSetting strength = register(new IntSetting("Strength", "Vignette strength", 100, 20, 255));
    private final IntSetting size = register(new IntSetting("Size", "Vignette size percent", 40, 10, 80));

    public TunnelVision() { super("TunnelVision", "Adds a vignette effect to edges of screen", Category.RENDER); }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        DrawContext ctx = e.getDrawContext();
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        int s = strength.get();
        int sz = size.get();
        // Draw dark borders on all 4 sides
        int bw = sw * sz / 100;
        int bh = sh * sz / 100;
        ctx.fillGradient(0, 0, bw, sh, ColorUtil.withAlpha(0x000000, s), ColorUtil.withAlpha(0x000000, 0));
        ctx.fillGradient(sw - bw, 0, sw, sh, ColorUtil.withAlpha(0x000000, 0), ColorUtil.withAlpha(0x000000, s));
        ctx.fillGradient(0, 0, sw, bh, ColorUtil.withAlpha(0x000000, s), ColorUtil.withAlpha(0x000000, 0));
        ctx.fillGradient(0, sh - bh, sw, sh, ColorUtil.withAlpha(0x000000, 0), ColorUtil.withAlpha(0x000000, s));
    }
}
