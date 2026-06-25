package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

public class AntiCrash extends Module {
    private final BoolSetting antiParticle = register(new BoolSetting("Anti Particle", "Block excessive particle packets", true));
    private final BoolSetting antiExplosion = register(new BoolSetting("Anti Explosion", "Block massive explosion packets", true));
    private int particleCount = 0;
    private long lastReset = 0;

    public AntiCrash() { super("AntiCrash", "Blocks malicious packets that could crash your client", Category.MISC); }

    @EventHandler
    public void onPacketReceive(EventPacketReceive e) {
        long now = System.currentTimeMillis();
        if (now - lastReset > 1000) { particleCount = 0; lastReset = now; }

        if (antiParticle.isEnabled() && e.getPacket() instanceof ParticleS2CPacket pkt) {
            particleCount++;
            if (particleCount > 500) {
                e.cancel();
                if (particleCount == 501) ChatUtil.warn("Blocked particle spam attack!");
            }
        }
    }
}
