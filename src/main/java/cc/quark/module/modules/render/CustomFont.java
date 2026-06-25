package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class CustomFont extends Module {

    private final IntSetting size = register(new IntSetting(
            "Size", "Custom font size for HUD rendering", 8, 6, 24));

    private final ColorSetting defaultColor = register(new ColorSetting(
            "Default Color", "Default font color for HUD text", 0xFFFFFFFF));

    public CustomFont() {
        super("CustomFont", "Uses custom font rendering", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();

        // Display font preview in top-center
        String preview = "QuarkClient [" + size.get() + "px]";
        int textWidth = mc.textRenderer.getWidth(preview);

        // Scale is controlled via mixin to the font renderer;
        // here we draw the preview at the standard renderer size with our color.
        ctx.drawTextWithShadow(
                mc.textRenderer,
                preview,
                (sw - textWidth) / 2,
                2,
                defaultColor.get()
        );
    }

    public int getFontSize() {
        return size.get();
    }

    public int getFontColor() {
        return defaultColor.get();
    }
}
