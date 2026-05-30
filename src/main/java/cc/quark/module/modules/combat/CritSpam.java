package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/**
 * CritSpam — attacks the nearest player with critical hits by checking that
 * the player is airborne and descending. Optionally adds a micro-jump when
 * grounded to maintain a crit sequence.
 */
public class CritSpam extends Module {

    private final DoubleSetting range     = register(new DoubleSetting("Range",     "Attack range in blocks",                    3.5, 1.0, 6.0));
    private final BoolSetting   microJump = register(new BoolSetting  ("MicroJump", "Small jump when grounded to maintain crits", true));

    public CritSpam() {
        super("CritSpam", "Attacks nearby players with critical hits using airborne descending attacks", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        LivingEntity target = findNearest();
        if (target == null) return;

        boolean isDescending = mc.player.getVelocity().y < 0;
        boolean isAirborne   = !mc.player.isOnGround();

        if (isAirborne && isDescending) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        } else if (microJump.isEnabled() && mc.player.isOnGround()) {
            mc.player.addVelocity(0, 0.42, 0);
        }
    }

    private LivingEntity findNearest() {
        LivingEntity nearest = null;
        double bestDist = range.get();
        if (mc.world == null) return null;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity p)) continue;
            if (p == mc.player) continue;
            double dist = mc.player.distanceTo(p);
            if (dist < bestDist) {
                bestDist = dist;
                nearest  = p;
            }
        }
        return nearest;
    }
}
