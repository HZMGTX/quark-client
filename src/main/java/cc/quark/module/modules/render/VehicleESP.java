package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Box;

public class VehicleESP extends Module {

    private final ColorSetting color = register(new ColorSetting("Color", "ESP color for vehicles", 0xFF55AAFF));

    public VehicleESP() {
        super("VehicleESP", "Highlights boats, minecarts, and horses through walls", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();
        float td = event.getTickDelta();

        for (Entity entity : mc.world.getEntities()) {
            if (entity.isRemoved()) continue;
            boolean isVehicle = entity instanceof BoatEntity
                    || entity instanceof AbstractMinecartEntity
                    || entity instanceof AbstractHorseEntity;
            if (!isVehicle) continue;

            double ex = entity.prevX + (entity.getX() - entity.prevX) * td;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * td;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * td;
            Box box = entity.getBoundingBox().offset(
                    ex - entity.getX(), ey - entity.getY(), ez - entity.getZ());

            RenderUtil.drawESPBox(event.getMatrixStack(), box, r, g, b, 0.9f, 1.5f);
            RenderUtil.drawFilledBox(event.getMatrixStack(), box, r, g, b, 0.10f);
        }
    }
}
