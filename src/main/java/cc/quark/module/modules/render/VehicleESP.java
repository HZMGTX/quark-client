package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class VehicleESP extends Module {

    private final ColorSetting color   = register(new ColorSetting("Color",   "ESP color for vehicles",             0xFF44DDFF));
    private final BoolSetting  tracers = register(new BoolSetting("Tracers",  "Draw tracer lines to vehicles",      false));

    public VehicleESP() {
        super("VehicleESP", "ESP for boats and minecarts", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();
        Vec3d origin = mc.player.getEyePos();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof BoatEntity) && !(e instanceof AbstractMinecartEntity)) continue;

            double ex = e.prevX + (e.getX() - e.prevX) * td;
            double ey = e.prevY + (e.getY() - e.prevY) * td;
            double ez = e.prevZ + (e.getZ() - e.prevZ) * td;
            Box box = e.getBoundingBox().offset(ex - e.getX(), ey - e.getY(), ez - e.getZ());
            RenderUtil.drawESPBox(m, box, color.getRedF(), color.getGreenF(), color.getBlueF(), color.getAlphaF(), 1.5f);

            if (tracers.isEnabled()) {
                Vec3d target = new Vec3d(ex, ey + e.getHeight() * 0.5, ez);
                RenderUtil.drawLine3D(m, origin, target, color.getRedF(), color.getGreenF(), color.getBlueF(), 0.7f, 1.0f);
            }
        }
    }
}
