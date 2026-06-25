package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * BloodEffect - Spawns red particle effects when entities take damage.
 */
public class BloodEffect extends Module {

    private final IntSetting count = register(new IntSetting(
            "Count", "Number of particles per hit", 5, 1, 20));
    private final BoolSetting onSelf = register(new BoolSetting(
            "OnSelf", "Also show particles on player self", false));

    private final Map<Integer, Float> lastHealth = new HashMap<>();

    public BloodEffect() {
        super("BloodEffect", "Plays blood particle effects when entities take damage", Category.RENDER);
    }

    @Override
    public void onEnable() {
        lastHealth.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == mc.player && !onSelf.isEnabled()) continue;

            int id = entity.getId();
            float prevHp = lastHealth.getOrDefault(id, living.getHealth());
            float curHp  = living.getHealth();

            if (curHp < prevHp) {
                // Spawn particles at entity position
                int n = count.get();
                for (int i = 0; i < n; i++) {
                    double ox = (Math.random() - 0.5) * 0.6;
                    double oy = Math.random() * living.getHeight();
                    double oz = (Math.random() - 0.5) * 0.6;
                    double vx = (Math.random() - 0.5) * 0.2;
                    double vy = Math.random() * 0.2;
                    double vz = (Math.random() - 0.5) * 0.2;
                    mc.world.addParticle(ParticleTypes.DAMAGE_INDICATOR,
                            living.getX() + ox, living.getY() + oy, living.getZ() + oz,
                            vx, vy, vz);
                }
            }
            lastHealth.put(id, curHp);
        }
    }
}
