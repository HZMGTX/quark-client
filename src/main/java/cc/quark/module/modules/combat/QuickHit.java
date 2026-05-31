package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/**
 * QuickHit - attacks at the precise moment the attack cooldown reaches 100%
 * to ensure every swing deals full damage, maximising DPS without bypassing
 * the server-side cooldown system.
 *
 * <p>By attacking at exactly the right tick the module outperforms manual
 * clicking which may overshoot the optimal window.
 */
public class QuickHit extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 3.5, 2.0, 6.0));

    private final DoubleSetting cooldownThreshold = register(new DoubleSetting(
            "Cooldown Threshold", "Attack cooldown % required (100 = fully charged)", 100.0, 90.0, 100.0));

    private final BoolSetting swing = register(new BoolSetting(
            "Swing", "Play hand swing animation", true));

    private final BoolSetting onlyWhenLooking = register(new BoolSetting(
            "Require LOS", "Only attack when crosshair is on the target", false));

    private final BoolSetting sprintReset = register(new BoolSetting(
            "Sprint Reset", "Briefly stop sprinting before attack for sprint-reset bonus", true));

    public QuickHit() {
        super("QuickHit", "Attacks at peak cooldown window for maximum damage per hit", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Wait until cooldown is at the configured threshold
        float cooldown = mc.player.getAttackCooldownProgress(0.0f);
        if (cooldown < (cooldownThreshold.get() / 100.0)) return;

        LivingEntity target = findNearestTarget();
        if (target == null) return;

        if (onlyWhenLooking.isEnabled() && mc.targetedEntity != target) return;

        // Sprint reset: briefly stop sprinting, hit, then restore
        boolean wasSprinting = mc.player.isSprinting();
        if (sprintReset.isEnabled() && wasSprinting) {
            mc.player.setSprinting(false);
        }

        mc.interactionManager.attackEntity(mc.player, target);
        if (swing.isEnabled()) mc.player.swingHand(Hand.MAIN_HAND);

        if (sprintReset.isEnabled() && wasSprinting) {
            mc.player.setSprinting(true);
        }
    }

    private LivingEntity findNearestTarget() {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        double r = range.get();

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity p)) continue;
            if (p.isDead() || p.getHealth() <= 0f) continue;
            double d = mc.player.getEyePos().distanceTo(e.getEyePos());
            if (d <= r && d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }
}
