package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

/**
 * Smash — on EventAttack (or auto-attack), sets the target entity's Y-velocity
 * to a negative value to force it downward — useful for knocking enemies off
 * ledges or into holes.
 */
public class Smash extends Module {

    private final DoubleSetting range     = register(new DoubleSetting("Range",     "Attack range",                     3.5, 1.0, 6.0));
    private final DoubleSetting downForce = register(new DoubleSetting("Down Force","Negative Y velocity applied",      -0.5, -2.0, -0.1));
    private final BoolSetting   autoHit   = register(new BoolSetting  ("Auto Hit",  "Auto-attack nearest target",        true));

    public Smash() {
        super("Smash", "Sets target's Y-velocity negative to knock them downward", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (event.getTarget() instanceof LivingEntity living) {
            applySmash(living);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!autoHit.isEnabled()) return;
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0f) < 1.0f) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isRemoved()) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            mc.interactionManager.attackEntity(mc.player, living);
            mc.player.swingHand(Hand.MAIN_HAND);
            applySmash(living);
            return;
        }
    }

    private void applySmash(LivingEntity target) {
        if (target == null) return;
        // Keep horizontal velocity; only override Y
        target.setVelocity(target.getVelocity().x, downForce.get(), target.getVelocity().z);
    }
}
