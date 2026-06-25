package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;

/**
 * CritAura — combines a packet-crit sequence with an auto-attack.
 * Sends three OnGround=false motion packets (micro-jump) so the server
 * registers a critical hit, then immediately attacks the nearest target.
 * Uses TimerUtil/cooldown so it does not fire more than once per tick cycle.
 */
public class CritAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range in blocks", 3.5, 1.0, 6.0));

    public CritAura() {
        super("CritAura", "Performs packet crits + auto attack on nearest target", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.getNetworkHandler() == null) return;
        if (mc.player.getAttackCooldownProgress(0f) < 1.0f) return;
        if (mc.player.isInLava() || mc.player.isTouchingWater() || mc.player.isClimbing()) return;

        // Find nearest target
        LivingEntity target = null;
        double best = range.get();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;
            double d = mc.player.distanceTo(entity);
            if (d < best) { best = d; target = living; }
        }
        if (target == null) return;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        // Packet crit: three micro-packets to fool the server into a crit register
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false));
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1.1E-5, z, false));
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true));

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
