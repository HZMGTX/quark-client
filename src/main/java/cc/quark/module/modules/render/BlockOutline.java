package cc.quark.module.modules.render;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;

public class BlockOutline extends Module {
    private final IntSetting r = register(new IntSetting("Red", "Outline red", 255, 0, 255));
    private final IntSetting g = register(new IntSetting("Green", "Outline green", 255, 0, 255));
    private final IntSetting b = register(new IntSetting("Blue", "Outline blue", 255, 0, 255));
    private final IntSetting width = register(new IntSetting("Width", "Line width", 2, 1, 5));
    private final BoolSetting filled = register(new BoolSetting("Filled", "Fill the block face", false));

    public BlockOutline() {
        super("Block Outline", "Customizable block selection outline", Category.RENDER, 0);
    }

    public int getRed()   { return r.get(); }
    public int getGreen() { return g.get(); }
    public int getBlue()  { return b.get(); }
    public int getWidth() { return width.get(); }
    public boolean isFilled() { return filled.isEnabled(); }
    // Actual rendering requires a mixin into WorldRenderer.drawBlockOutline()
    // This module stores the settings for that mixin to use
}
