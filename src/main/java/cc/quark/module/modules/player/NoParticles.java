package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.*;

public class NoParticles extends Module {

    private final BoolSetting allParticles    = register(new BoolSetting("All",       "Cancel all server-sent particle packets", false));
    private final BoolSetting blockParticles  = register(new BoolSetting("Block",     "Cancel block-type particles (dust, falling)", true));
    private final BoolSetting mobParticles    = register(new BoolSetting("Mob",       "Cancel mob status effect particles", true));
    private final BoolSetting ambientParticles = register(new BoolSetting("Ambient",  "Cancel ambient/environmental particles", false));
    private final BoolSetting explosions      = register(new BoolSetting("Explosion", "Cancel explosion particles", false));

    public NoParticles() {
        super("NoParticles", "Cancels server-sent particle packets to reduce visual clutter and improve FPS", Category.PLAYER);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof ParticleS2CPacket pkt)) return;

        if (allParticles.isEnabled()) {
            event.cancel();
            return;
        }

        ParticleEffect effect = pkt.getParameters();

        if (blockParticles.isEnabled()) {
            if (effect instanceof BlockStateParticleEffect
                    || effect.getType() == ParticleTypes.DUST
                    || effect.getType() == ParticleTypes.DUST_COLOR_TRANSITION
                    || effect.getType() == ParticleTypes.FALLING_DUST) {
                event.cancel();
                return;
            }
        }

        if (mobParticles.isEnabled()) {
            if (effect.getType() == ParticleTypes.EFFECT
                    || effect.getType() == ParticleTypes.INSTANT_EFFECT
                    || effect.getType() == ParticleTypes.ENTITY_EFFECT
                    || effect.getType() == ParticleTypes.ENTITY_EFFECT
                    || effect.getType() == ParticleTypes.WITCH
                    || effect.getType() == ParticleTypes.ANGRY_VILLAGER
                    || effect.getType() == ParticleTypes.HAPPY_VILLAGER) {
                event.cancel();
                return;
            }
        }

        if (ambientParticles.isEnabled()) {
            if (effect.getType() == ParticleTypes.MYCELIUM
                    || effect.getType() == ParticleTypes.RAIN
                    || effect.getType() == ParticleTypes.DRIPPING_WATER
                    || effect.getType() == ParticleTypes.DRIPPING_LAVA
                    || effect.getType() == ParticleTypes.SMOKE
                    || effect.getType() == ParticleTypes.LARGE_SMOKE
                    || effect.getType() == ParticleTypes.FLAME
                    || effect.getType() == ParticleTypes.LAVA) {
                event.cancel();
                return;
            }
        }

        if (explosions.isEnabled()) {
            if (effect.getType() == ParticleTypes.EXPLOSION
                    || effect.getType() == ParticleTypes.EXPLOSION_EMITTER
                    || effect.getType() == ParticleTypes.POOF) {
                event.cancel();
            }
        }
    }
}
