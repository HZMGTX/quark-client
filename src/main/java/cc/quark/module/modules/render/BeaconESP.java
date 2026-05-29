package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * BeaconESP - draws a tall vertical beam (beacon-style) above each player
 * for long-range spotting.
 */
public class BeaconESP extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Maximum beam render distance", 256, 32, 512));

    private final ColorSetting color = register(new ColorSetting(
            "Color", "Beam color (ARGB)", 0x80FF4040));

    private final DoubleSetting beamHeight = register(new DoubleSetting(
            "Beam Height", "Height of the beacon beam in blocks", 32, 4, 128));

    public BeaconESP() {
        super("BeaconESP", "Draws a beacon beam above players for easy long-range spotting", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();
        float a = color.getAlphaF();
        double maxRng = range.get();
        double height  = beamHeight.get();
        float td = event.getTickDelta();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity)) continue;
            if (entity.isInvisible()) continue;

            double ex = entity.prevX + (entity.getX() - entity.prevX) * td;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * td;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * td;

            Vec3d center = new Vec3d(ex, ey, ez);
            if (mc.player.getPos().distanceTo(center) > maxRng) continue;

            // Draw a thin filled box as the beam
            double hw = 0.15; // half-width of the beam
            Box beam = new Box(ex - hw, ey + entity.getHeight(), ez - hw,
                               ex + hw, ey + entity.getHeight() + height, ez + hw);

            RenderUtil.drawFilledBox(event.getMatrixStack(), beam, r, g, b, a * 0.4f);
            RenderUtil.drawESPBox(event.getMatrixStack(), beam, r, g, b, a, 1.0f);
        }
    }
}
