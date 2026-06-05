package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;

public class Crosshair extends Module {
    private final ModeSetting style = register(new ModeSetting("Style", "Crosshair style", "Classic", "Classic", "Dot", "Circle", "Cross", "None"));
    private final ColorSetting color = register(new ColorSetting("Color", "Crosshair color", 0xFFFFFFFF));
    private final IntSetting size = register(new IntSetting("Size", "Crosshair size", 5, 1, 20));
    private final IntSetting thickness = register(new IntSetting("Thickness", "Line thickness", 1, 1, 5));

    public Crosshair() { super("Crosshair", "Custom crosshair styles and colors", Category.RENDER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        DrawContext ctx = e.getDrawContext();
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int cx = sw / 2, cy = sh / 2;
        int sz = size.get(), th = thickness.get();
        int c = ColorUtil.fromSettingARGB(color.getRedF(), color.getGreenF(), color.getBlueF(), color.getAlphaF());

        switch (style.get()) {
            case "Classic" -> {
                ctx.fill(cx - sz, cy - th/2, cx + sz, cy + th/2, c);
                ctx.fill(cx - th/2, cy - sz, cx + th/2, cy + sz, c);
            }
            case "Dot" -> ctx.fill(cx - th, cy - th, cx + th, cy + th, c);
            case "Cross" -> {
                ctx.fill(cx - sz, cy - th/2, cx - 3, cy + th/2, c);
                ctx.fill(cx + 3, cy - th/2, cx + sz, cy + th/2, c);
                ctx.fill(cx - th/2, cy - sz, cx + th/2, cy - 3, c);
                ctx.fill(cx - th/2, cy + 3, cx + th/2, cy + sz, c);
            }
        }
    }
}
