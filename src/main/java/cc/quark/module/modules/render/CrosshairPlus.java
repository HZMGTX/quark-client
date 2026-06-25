package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.gui.DrawContext;

public class CrosshairPlus extends Module {
    private final ModeSetting  style = register(new ModeSetting ("Style","Crosshair style","Cross","Cross","Dot","Circle","Arrow"));
    private final IntSetting   size  = register(new IntSetting  ("Size","Crosshair size",8,2,24));
    private final ColorSetting color = register(new ColorSetting("Color","Crosshair color",0xFFFFFFFF));

    public CrosshairPlus() { super("CrosshairPlus","Custom configurable crosshair overlay",Category.RENDER); }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player==null) return;
        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth()/2;
        int sh = mc.getWindow().getScaledHeight()/2;
        int s = size.get(); int c = color.get();
        switch (style.get()) {
            case "Cross" -> { ctx.fill(sw-s,sh-1,sw+s,sh+1,c); ctx.fill(sw-1,sh-s,sw+1,sh+s,c); }
            case "Dot"   -> ctx.fill(sw-2,sh-2,sw+2,sh+2,c);
            case "Circle" -> {
                for (int i=0;i<36;i++) {
                    double a=Math.toRadians(i*10);
                    int px=sw+(int)(Math.cos(a)*s); int py=sh+(int)(Math.sin(a)*s);
                    ctx.fill(px,py,px+1,py+1,c);
                }
            }
            default -> ctx.fill(sw-s,sh-1,sw+s,sh+1,c);
        }
    }
}
