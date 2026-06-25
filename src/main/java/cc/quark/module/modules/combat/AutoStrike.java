package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

public class AutoStrike extends Module {

    private final DoubleSetting cooldown = register(new DoubleSetting(
            "Cooldown", "Required cooldown fraction before striking (0.0-1.0)", 1.0, 0.5, 1.0));

    public AutoStrike() {
        super("AutoStrike", "Attacks with perfect timing for max damage", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        float progress = mc.player.getAttackCooldownProgress(0f);
        if (progress < (float) cooldown.get()) return;

        LivingEntity target = null;
        double closestDist = 4.5;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player || living.isRemoved()) continue;
            double dist = mc.player.distanceTo(living);
            if (dist < closestDist) {
                closestDist = dist;
                target = living;
            }
        }

        if (target != null) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
