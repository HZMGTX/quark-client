package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;

public class RainbowAccent extends Module {

    private final DoubleSetting speed      = register(new DoubleSetting("Speed",      "Hue cycle speed",       1.0, 0.1, 10.0));
    private final IntSetting    saturation = register(new IntSetting("Saturation",    "Color saturation 0-255", 255, 0, 255));

    /** Current hue in [0, 1). */
    private float hue = 0f;

    public RainbowAccent() {
        super("RainbowAccent", "Animates accent color through rainbow", Category.RENDER);
    }

    @Override
    public void onEnable() {
        hue = 0f;
    }

    @EventHandler
    public void onTick(EventTick event) {
        hue = (hue + (float)(speed.get() * 0.005)) % 1.0f;
    }

    /** Returns the current rainbow color as a packed ARGB integer. */
    public int getCurrentColor() {
        float sat = saturation.get() / 255f;
        int rgb = java.awt.Color.HSBtoRGB(hue, sat, 1.0f);
        return 0xFF000000 | (rgb & 0x00FFFFFF);
    }

    /** Returns the current hue in [0, 1). */
    public float getHue() {
        return hue;
    }

    @Override
    public String getSuffix() {
        int r = (getCurrentColor() >> 16) & 0xFF;
        int g = (getCurrentColor() >> 8)  & 0xFF;
        int b = getCurrentColor() & 0xFF;
        return String.format("#%02X%02X%02X", r, g, b);
    }
}
