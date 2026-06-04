package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.TntEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

/**
 * ExplosionEffect - Enhances explosion visuals by spawning extra particles near TNT
 * entities and recently exploded positions.
 */
public class ExplosionEffect extends Module {

    private final IntSetting particles = register(new IntSetting(
            "Particles", "Extra particles per tick near TNT", 20, 1, 50));
    private final BoolSetting bigExplosion = register(new BoolSetting(
            "BigExplosion", "Spawn large explosion particles", true));

    private final Map<Integer, Vec3d> trackedTnt = new HashMap<>();

    public ExplosionEffect() {
        super("ExplosionEffect", "Enhanced explosion visual effects", Category.RENDER);
    }

    @Override
    public void onEnable() {
        trackedTnt.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof TntEntity tnt)) continue;

            Vec3d pos = tnt.getPos();
            int n = particles.get();

            for (int i = 0; i < n; i++) {
                double ox = (Math.random() - 0.5) * 1.5;
                double oy = Math.random() * 1.5;
                double oz = (Math.random() - 0.5) * 1.5;
                double vx = (Math.random() - 0.5) * 0.1;
                double vy = Math.random() * 0.15;
                double vz = (Math.random() - 0.5) * 0.1;

                mc.world.addParticle(
                        bigExplosion.isEnabled() ? ParticleTypes.EXPLOSION : ParticleTypes.FLAME,
                        pos.x + ox, pos.y + oy, pos.z + oz, vx, vy, vz);
            }

            // Fuse countdown sparks
            mc.world.addParticle(ParticleTypes.FLAME, pos.x, pos.y + 0.5, pos.z, 0, 0.1, 0);
        }
    }
}
