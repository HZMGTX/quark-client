package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * FontRenderer - Uses custom font-rendering options for HUD overlay elements.
 * Draws a configurable demo string on-screen using Minecraft's text renderer
 * with scale, shadow, and color adjustments.
 *
 * To use: hook your own HUD elements into the DrawContext provided via
 * EventRender2D, or adjust the built-in preview string.
 */
public class FontRenderer extends Module {

    private final StringSetting previewText = register(new StringSetting(
            "Preview Text", "String rendered on the HUD as a font preview", "Quark.cc"));

    private final IntSetting posX = register(new IntSetting(
            "X", "Horizontal position of the preview text", 4, 0, 1920));

    private final IntSetting posY = register(new IntSetting(
            "Y", "Vertical position of the preview text", 4, 0, 1080));

    private final DoubleSetting scale = register(new DoubleSetting(
            "Scale", "Text scale multiplier", 1.0, 0.5, 4.0));

    private final BoolSetting shadow = register(new BoolSetting(
            "Shadow", "Render text with drop shadow", true));

    private final IntSetting red   = register(new IntSetting("Red",   "Red channel (0-255)",   255, 0, 255));
    private final IntSetting green = register(new IntSetting("Green", "Green channel (0-255)", 255, 0, 255));
    private final IntSetting blue  = register(new IntSetting("Blue",  "Blue channel (0-255)",  255, 0, 255));

    private final BoolSetting rainbow = register(new BoolSetting(
            "Rainbow", "Cycle through rainbow colors", false));

    private float hue = 0f;

    public FontRenderer() {
        super("FontRenderer", "Renders HUD text using custom font-rendering settings", Category.MISC);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext context = event.getDrawContext();
        TextRenderer tr = mc.textRenderer;

        String text = previewText.get();
        if (text.isEmpty()) return;

        int color;
        if (rainbow.isEnabled()) {
            hue = (hue + 0.01f) % 1.0f;
            color = 0xFF000000 | java.awt.Color.HSBtoRGB(hue, 1f, 1f);
        } else {
            color = 0xFF000000
                    | ((red.get()   & 0xFF) << 16)
                    | ((green.get() & 0xFF) << 8)
                    |  (blue.get()  & 0xFF);
        }

        float s = (float) scale.get();
        context.getMatrices().push();
        context.getMatrices().scale(s, s, 1.0f);

        int sx = (int) (posX.get() / s);
        int sy = (int) (posY.get() / s);

        if (shadow.isEnabled()) {
            context.drawTextWithShadow(tr, text, sx, sy, color);
        } else {
            context.drawText(tr, text, sx, sy, color, false);
        }

        context.getMatrices().pop();
    }
}
