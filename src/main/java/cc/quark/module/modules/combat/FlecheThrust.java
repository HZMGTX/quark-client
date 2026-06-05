package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class FlecheThrust extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to perform fleche thrust attack", 3.5, 1.0, 6.0));

    private final DoubleSetting sprintBoost = register(new DoubleSetting(
            "Sprint Boost", "Additional velocity boost during thrust", 0.5, 0.0, 2.0));

    private boolean thrusting = false;

    public FlecheThrust() {
        super("FlecheThrust", "Sprint-thrust attack for extra damage", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        thrusting = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0f) < 1f) return;

        double r = range.get();
        LivingEntity target = null;
        double closestDist = Double.MAX_VALUE;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player || living.isRemoved()) continue;
            double dist = mc.player.distanceTo(living);
            if (dist <= r && dist < closestDist) {
                closestDist = dist;
                target = living;
            }
        }

        if (target == null) { thrusting = false; return; }

        if (!thrusting) {
            // Initiate sprint-thrust
            Vec3d dir = target.getPos().subtract(mc.player.getPos()).normalize();
            mc.player.setVelocity(
                    mc.player.getVelocity().add(dir.x * sprintBoost.get(), 0, dir.z * sprintBoost.get()));
            mc.player.setSprinting(true);
            thrusting = true;
        } else {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            thrusting = false;
        }
    }
}
