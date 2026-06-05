package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * ForceField — pushes all entities within Radius away from the player.
 * Applies reversed velocity (entity pos minus player pos, normalised × Force)
 * to every entity in range every Pulse ticks.
 * Also optionally attacks them (attack mode).
 */
public class ForceField extends Module {

    private final DoubleSetting radius  = register(new DoubleSetting("Radius", "Force-field radius",                        4.0, 1.0, 10.0));
    private final DoubleSetting force   = register(new DoubleSetting("Force",  "Push velocity magnitude",                   0.5, 0.1,  3.0));
    private final IntSetting    pulse   = register(new IntSetting   ("Pulse",  "Ticks between force pulses",                4,   1,   20));

    private int ticker = 0;

    public ForceField() {
        super("ForceField", "Pushes all nearby entities away from the player every pulse", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (++ticker < pulse.get()) return;
        ticker = 0;

        double r = radius.get();
        double f = force.get();
        boolean didHit = false;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;
            if (mc.player.distanceTo(entity) > r) continue;

            // Attack to deal damage (the main "force-field" effect)
            mc.interactionManager.attackEntity(mc.player, entity);
            didHit = true;

            // Apply outward velocity
            Vec3d dir = entity.getPos().subtract(mc.player.getPos());
            double len = dir.length();
            if (len > 0.001) {
                Vec3d push = dir.multiply(f / len);
                entity.setVelocity(entity.getVelocity().add(push));
            }
        }

        if (didHit) mc.player.swingHand(Hand.MAIN_HAND);
    }
}
