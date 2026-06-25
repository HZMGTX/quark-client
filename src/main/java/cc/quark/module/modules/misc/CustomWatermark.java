package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.client.gui.DrawContext;

public class CustomWatermark extends Module {

    private final StringSetting text   = register(new StringSetting("Text",  "Watermark text to display",    "Quark.cc"));
    private final ColorSetting  color  = register(new ColorSetting("Color",  "Watermark text color",         0xFFAA55FF));
    private final IntSetting    posX   = register(new IntSetting("X",        "HUD X position",               4, 0, 500));
    private final IntSetting    posY   = register(new IntSetting("Y",        "HUD Y position",               4, 0, 500));

    public CustomWatermark() {
        super("CustomWatermark", "Renders a custom watermark text in the corner of the screen", Category.MISC);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        String display = text.get();
        if (display.isEmpty()) return;
        DrawContext ctx = event.getDrawContext();
        ctx.drawTextWithShadow(mc.textRenderer, display, posX.get(), posY.get(), color.get());
    }
}
