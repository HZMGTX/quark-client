package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * TrailEffect - Records recent player positions and draws a 2D tail on the HUD,
 * and spawns particles in the world as the player moves.
 */
public class TrailEffect extends Module {

    private final IntSetting particle = register(new IntSetting(
            "Particle", "Particle type index (0=flame, 1=note, 2=heart)", 0, 0, 2));
    private final ColorSetting color = register(new ColorSetting(
            "Color", "Trail line color", 0xFF00AAFF));

    private final Deque<Vec3d> trail = new ArrayDeque<>();
    private static final int MAX_POINTS = 30;
    private Vec3d lastPos = null;

    public TrailEffect() {
        super("TrailEffect", "Leaves particle trail while moving", Category.RENDER);
    }

    @Override
    public void onEnable() {
        trail.clear();
        lastPos = null;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        Vec3d pos = mc.player.getPos();
        if (lastPos == null || pos.squaredDistanceTo(lastPos) > 0.01) {
            trail.addFirst(pos);
            while (trail.size() > MAX_POINTS) trail.removeLast();

            // Spawn world particle
            if (mc.world != null) {
                var pType = switch (particle.get()) {
                    case 1  -> ParticleTypes.NOTE;
                    case 2  -> ParticleTypes.HEART;
                    default -> ParticleTypes.FLAME;
                };
                mc.world.addParticle(pType, pos.x, pos.y + 0.1, pos.z, 0, 0.05, 0);
            }
            lastPos = pos;
        }
    }
}
