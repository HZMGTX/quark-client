package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * AntiFireball — finds nearby fireball/wither-skull entities and deflects them
 * by punching (which reverses the projectile's velocity server-side).
 * Also applies a small push-back velocity to keep the entity moving away.
 */
public class AntiFireball extends Module {

    private final DoubleSetting range      = register(new DoubleSetting("Range",       "Deflect range in blocks",             3.0, 1.0, 6.0));
    private final BoolSetting   witherSkull = register(new BoolSetting("Wither Skull", "Also deflect wither skulls",           true));
    private final BoolSetting   pushAway    = register(new BoolSetting("Push Away",    "Add extra velocity away from player",  true));

    public AntiFireball() {
        super("AntiFireball", "Deflects incoming fireballs and wither skulls", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        for (Entity entity : mc.world.getEntities()) {
            boolean isFireball    = entity instanceof AbstractFireballEntity;
            boolean isWitherSkull = entity instanceof WitherSkullEntity;

            if (!isFireball && !isWitherSkull) continue;
            if (isWitherSkull && !witherSkull.isEnabled()) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;

            // Punch to deflect
            mc.interactionManager.attackEntity(mc.player, entity);
            mc.player.swingHand(Hand.MAIN_HAND);

            // Apply additional away-velocity so it doesn't linger
            if (pushAway.isEnabled()) {
                Vec3d dir = entity.getPos().subtract(mc.player.getPos()).normalize();
                entity.setVelocity(dir.multiply(1.5));
            }
            return; // one per tick is enough
        }
    }
}
