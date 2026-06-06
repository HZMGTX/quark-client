package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * GhastAura — automatically deflects ghast fireballs and attacks nearby ghasts.
 * Deflection is achieved by punching the fireball entity (which reflects it back).
 */
public class GhastAura extends Module {

    private final DoubleSetting attackRange = register(new DoubleSetting(
            "Attack Range", "Melee range to attack ghasts", 5.0, 1.0, 10.0));
    private final DoubleSetting deflectRange = register(new DoubleSetting(
            "Deflect Range", "Range to deflect incoming fireballs", 4.0, 1.0, 8.0));
    private final IntSetting attackDelay = register(new IntSetting(
            "Delay", "Milliseconds between ghast attacks", 300, 100, 1000));
    private final BoolSetting deflect = register(new BoolSetting(
            "Deflect", "Reflect ghast fireballs back toward the ghast", true));
    private final BoolSetting attackGhasts = register(new BoolSetting(
            "Attack Ghasts", "Melee-attack nearby ghasts", true));

    private final TimerUtil timer = new TimerUtil();

    public GhastAura() {
        super("GhastAura", "Attacks ghasts and reflects their fireballs", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
        timer.reset();
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Deflect incoming fireballs
        if (deflect.isEnabled()) {
            for (Entity entity : mc.world.getEntities()) {
                if (!(entity instanceof FireballEntity fireball)) continue;
                if (mc.player.distanceTo(fireball) > deflectRange.get()) continue;

                // Punch the fireball to deflect it
                mc.interactionManager.attackEntity(mc.player, fireball);
                mc.player.swingHand(Hand.MAIN_HAND);

                // Push it away from the player back toward the shooter
                Vec3d dir = fireball.getPos().subtract(mc.player.getPos()).normalize();
                fireball.setVelocity(dir.multiply(1.5));
                return; // one deflect per tick
            }
        }

        // Attack nearby ghasts
        if (attackGhasts.isEnabled() && timer.hasReached(attackDelay.get())) {
            GhastEntity closest = null;
            double bestDist = attackRange.get();

            for (Entity entity : mc.world.getEntities()) {
                if (!(entity instanceof GhastEntity ghast)) continue;
                if (ghast.isRemoved() || ghast.getHealth() <= 0f) continue;
                double d = mc.player.distanceTo(ghast);
                if (d < bestDist) {
                    bestDist = d;
                    closest = ghast;
                }
            }

            if (closest != null) {
                mc.interactionManager.attackEntity(mc.player, closest);
                mc.player.swingHand(Hand.MAIN_HAND);
                timer.reset();
            }
        }
    }
}
