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
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Cleave — on EventAttack, also attacks all other living entities within
 * ConeAngle in front of the player (up to MaxTargets extra targets).
 * Falls back to a simple range sweep on EventTick when no manual attack fires.
 */
public class Cleave extends Module {

    private final DoubleSetting range     = register(new DoubleSetting("Range",      "Attack range in blocks",        3.5, 1.0, 6.0));
    private final IntSetting    max       = register(new IntSetting   ("Max Targets","Max extra targets per swing",    3, 1, 8));
    private final DoubleSetting coneAngle = register(new DoubleSetting("Cone Angle", "Half-angle of the cone (degrees)", 60.0, 10.0, 180.0));

    public Cleave() {
        super("Cleave", "Attacks multiple targets in front of you on each swing", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        attackCone(event.getTarget());
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0f) < 1.0f) return;
        attackCone(null);
    }

    private void attackCone(Entity primary) {
        List<LivingEntity> targets = new ArrayList<>();
        float playerYaw = mc.player.getYaw();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (entity == primary) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;

            // Cone check
            double dx = entity.getX() - mc.player.getX();
            double dz = entity.getZ() - mc.player.getZ();
            float yawToEntity = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float angle = Math.abs(MathHelper.wrapDegrees(yawToEntity - playerYaw));
            if (angle > coneAngle.get()) continue;

            targets.add(living);
        }

        targets.sort(Comparator.comparingDouble(e -> mc.player.distanceTo(e)));

        int count = Math.min(max.get(), targets.size());
        for (int i = 0; i < count; i++) {
            mc.interactionManager.attackEntity(mc.player, targets.get(i));
        }
        if (count > 0) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
