package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;

public class BloodEffect extends Module {

    private final ModeSetting style = register(new ModeSetting("Style", "Particle style on hit", "Normal", "Normal", "Crit", "Splash"));

    public BloodEffect() {
        super("BloodEffect", "Plays a particle burst on successful hit", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null || mc.world == null) return;
        Entity target = event.getTarget();
        if (target == null) return;

        double x = target.getX();
        double y = target.getY() + target.getHeight() / 2.0;
        double z = target.getZ();

        switch (style.get()) {
            case "Normal" -> {
                for (int i = 0; i < 8; i++) {
                    mc.world.addParticle(ParticleTypes.DAMAGE_INDICATOR, x, y, z,
                            (Math.random() - 0.5) * 0.5, Math.random() * 0.3, (Math.random() - 0.5) * 0.5);
                }
            }
            case "Crit" -> {
                for (int i = 0; i < 10; i++) {
                    mc.world.addParticle(ParticleTypes.CRIT, x, y, z,
                            (Math.random() - 0.5) * 0.6, Math.random() * 0.4, (Math.random() - 0.5) * 0.6);
                }
            }
            case "Splash" -> {
                for (int i = 0; i < 12; i++) {
                    mc.world.addParticle(ParticleTypes.SPLASH, x, y, z,
                            (Math.random() - 0.5) * 0.8, Math.random() * 0.2, (Math.random() - 0.5) * 0.8);
                }
            }
        }
    }
}
