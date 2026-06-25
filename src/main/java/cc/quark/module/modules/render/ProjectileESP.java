package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.*;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ProjectileESP extends Module {

    private final BoolSetting tracers = register(new BoolSetting("Tracers", "Draw tracer lines to projectiles", true));

    public ProjectileESP() {
        super("ProjectileESP", "ESP boxes and optional tracers for arrows, tridents, and fireballs", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();
        Vec3d origin = mc.player.getEyePos();

        for (Entity e : mc.world.getEntities()) {
            float r, g, b;
            if (e instanceof ArrowEntity || e instanceof SpectralArrowEntity) {
                r = 1.0f; g = 0.9f; b = 0.4f;
            } else if (e instanceof TridentEntity) {
                r = 0.4f; g = 0.7f; b = 1.0f;
            } else if (e instanceof FireballEntity || e instanceof SmallFireballEntity || e instanceof WitherSkullEntity) {
                r = 1.0f; g = 0.4f; b = 0.1f;
            } else {
                continue;
            }

            double ex = e.prevX + (e.getX() - e.prevX) * td;
            double ey = e.prevY + (e.getY() - e.prevY) * td;
            double ez = e.prevZ + (e.getZ() - e.prevZ) * td;
            Box box = e.getBoundingBox().offset(ex - e.getX(), ey - e.getY(), ez - e.getZ());
            RenderUtil.drawESPBox(m, box, r, g, b, 0.9f, 1.5f);

            if (tracers.isEnabled()) {
                Vec3d target = new Vec3d(ex, ey + e.getHeight() * 0.5, ez);
                RenderUtil.drawLine3D(m, origin, target, r, g, b, 0.7f, 1.0f);
            }
        }
    }
}
