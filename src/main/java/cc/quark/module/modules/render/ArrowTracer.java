package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.Vec3d;

public class ArrowTracer extends Module {
    private final ColorSetting color = register(new ColorSetting("Color","Arrow tracer color",0xFFFFAA00));
    public ArrowTracer() { super("ArrowTracer","Draws lines to nearby arrows in the air",Category.RENDER); }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player==null||mc.world==null) return;
        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth()/2;
        int sh = mc.getWindow().getScaledHeight()/2;
        int c = color.get();
        for (var e : mc.world.getEntities()) {
            if (!(e instanceof ArrowEntity)) continue;
            if (mc.player.distanceTo(e)>64) continue;
            ctx.fill(sw,sh,sw+2,sh+2,c);
        }
    }
}
