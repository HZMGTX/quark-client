package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.client.gui.DrawContext;

public class NightMode extends Module {

    private final DoubleSetting darkness = register(new DoubleSetting(
            "Darkness", "Opacity of the night overlay", 0.5, 0.0, 1.0));

    private final ColorSetting tint = register(new ColorSetting(
            "Tint", "Color tint for night aesthetic", 0x55000033));

    public NightMode() {
        super("NightMode", "Darkens the screen for night aesthetic", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        int alpha = (int)(darkness.get() * 180);
        int baseColor = (alpha << 24);
        ctx.fill(0, 0, sw, sh, baseColor);

        // Apply color tint
        int tintColor = tint.get();
        ctx.fill(0, 0, sw, sh, tintColor);
    }
}
