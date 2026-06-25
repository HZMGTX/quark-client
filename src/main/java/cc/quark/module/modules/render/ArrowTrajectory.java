package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.math.Vec3d;

/**
 * ArrowTrajectory - predicts and renders the flight path of an arrow when holding
 * a bow or crossbow, using a simple ballistics simulation.
 *
 * <p>Complements the more general {@link Trajectories} module with arrow-specific
 * gravity/drag tuning and a distinct color scheme.
 */
public class ArrowTrajectory extends Module {

    private final IntSetting steps = register(new IntSetting(
            "Steps", "Simulation steps (higher = longer path)", 80, 20, 200));
    private final BoolSetting showCrossbow = register(new BoolSetting(
            "Crossbow", "Also predict crossbow bolt trajectory", true));

    public ArrowTrajectory() {
        super("Arrow Trajectory", "Shows predicted arrow flight path for bows and crossbows", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;

        var held = mc.player.getMainHandStack();
        var item = held.getItem();

        double speed;
        double drag = 0.99;
        double gravity = 0.05;

        if (item instanceof BowItem) {
            speed = 3.0;
        } else if (item instanceof CrossbowItem && showCrossbow.isEnabled()) {
            speed = 3.15;
        } else {
            return;
        }

        float yaw   = (float) Math.toRadians(mc.player.getYaw());
        float pitch = (float) Math.toRadians(mc.player.getPitch());

        double vx = -Math.sin(yaw) * Math.cos(pitch) * speed;
        double vy = -Math.sin(pitch) * speed;
        double vz =  Math.cos(yaw)  * Math.cos(pitch) * speed;

        Vec3d pos = mc.player.getEyePos();

        for (int i = 0; i < steps.get() - 1; i++) {
            Vec3d next = pos.add(vx, vy, vz);

            vx *= drag;
            vy -= gravity;
            vz *= drag;

            // Fade from yellow to red over time to indicate distance
            float t = (float) i / steps.get();
            float r = 1.0f;
            float g = 1.0f - t;
            float b = 0.0f;

            // Stop at solid blocks
            if (mc.world.getBlockState(new net.minecraft.util.math.BlockPos(
                    (int) Math.floor(next.x),
                    (int) Math.floor(next.y),
                    (int) Math.floor(next.z))).isSolid()) break;

            RenderUtil.drawLine3D(event.getMatrixStack(), pos, next, r, g, b, 0.85f, 1.5f);
            pos = next;
        }
    }
}
