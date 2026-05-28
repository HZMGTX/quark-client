package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
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

public class ComboKeeper extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range in blocks", 3.5, 1.0, 6.0));
    private final IntSetting targetCombo = register(new IntSetting("Target Combo", "Consecutive hits to maintain", 5, 3, 20));

    private int currentCombo = 0;
    private LivingEntity comboTarget = null;

    public ComboKeeper() {
        super("ComboKeeper", "Maintains hit combos in PvP", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        currentCombo = 0;
        comboTarget = null;
    }

    @Override
    public void onDisable() {
        currentCombo = 0;
        comboTarget = null;
    }

    @Override
    public String getSuffix() {
        return "Combo: " + currentCombo;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (!(event.getTarget() instanceof LivingEntity living)) return;
        if (comboTarget == null || comboTarget != living) {
            comboTarget = living;
            currentCombo = 0;
        }
        currentCombo++;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (comboTarget != null && (comboTarget.isDead() || comboTarget.getHealth() <= 0f
                || mc.player.distanceTo(comboTarget) > range.get() + 3.0)) {
            currentCombo = 0;
            comboTarget = null;
        }

        if (mc.player.getAttackCooldownProgress(0.0f) < 1.0f) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        if (currentCombo >= targetCombo.get() && mc.player.isSprinting()) {
            Vec3d toTarget = target.getPos().subtract(mc.player.getPos()).normalize();
            mc.player.setVelocity(
                mc.player.getVelocity().add(toTarget.x * 0.04, 0, toTarget.z * 0.04)
            );
        }

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private LivingEntity findTarget() {
        if (comboTarget != null && !comboTarget.isDead() && comboTarget.getHealth() > 0f
                && mc.player.distanceTo(comboTarget) <= range.get()) {
            return comboTarget;
        }

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist > range.get() || dist >= bestDist) continue;
            bestDist = dist;
            best = living;
        }
        return best;
    }
}
