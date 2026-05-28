package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;

public class LifeSteal extends Module {

    private final DoubleSetting ratio = register(new DoubleSetting("Ratio", "Fraction of damage dealt to heal back", 0.3, 0.1, 1.0));
    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range in blocks", 3.5, 1.0, 6.0));
    private final BoolSetting showParticles = register(new BoolSetting("Show Particles", "Spawn heart particles on heal", true));

    private float lastTargetHealth = -1f;
    private LivingEntity lastTarget = null;

    public LifeSteal() {
        super("LifeSteal", "Heals player based on damage dealt", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastTargetHealth = -1f;
        lastTarget = null;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (!(event.getTarget() instanceof LivingEntity living)) return;
        lastTargetHealth = living.getHealth();
        lastTarget = living;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (lastTarget != null && lastTargetHealth >= 0f) {
            float currentHealth = lastTarget.getHealth();
            float damageDealt = lastTargetHealth - currentHealth;
            if (damageDealt > 0f) {
                float healAmount = (float)(damageDealt * ratio.get());
                mc.player.heal(healAmount);

                if (showParticles.isEnabled()) {
                    for (int i = 0; i < 5; i++) {
                        double ox = (Math.random() - 0.5) * 0.5;
                        double oy = Math.random() * 1.0;
                        double oz = (Math.random() - 0.5) * 0.5;
                        mc.world.addParticle(
                            ParticleTypes.HEART,
                            mc.player.getX() + ox,
                            mc.player.getY() + oy,
                            mc.player.getZ() + oz,
                            0, 0, 0
                        );
                    }
                }
            }
            lastTargetHealth = -1f;
            lastTarget = null;
        }
    }
}
