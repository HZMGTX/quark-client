package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * SpeedAura — attacks the nearest player with a sprint-reset pattern while
 * optionally strafing perpendicular for unpredictability.
 */
public class SpeedAura extends Module {

    private final DoubleSetting range          = register(new DoubleSetting("Range",          "Attack range in blocks",              3.5, 1.0, 6.0));
    private final DoubleSetting strafeSpeed    = register(new DoubleSetting("StrafeSpeed",    "Perpendicular strafe speed",          0.4, 0.1, 1.0));
    private final BoolSetting   strafe         = register(new BoolSetting  ("Strafe",         "Strafe perpendicular after attacks",  true));
    private final IntSetting    attacksPerCycle = register(new IntSetting  ("AttacksPerCycle","Attacks per cycle before strafing",   2,   1,   5));

    private int attackCount  = 0;
    private int strafeDir    = 1; // 1 or -1

    public SpeedAura() {
        super("SpeedAura", "Attacks nearby players with sprint-reset combos and perpendicular strafing", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        attackCount = 0;
        strafeDir   = 1;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        LivingEntity target = findNearest();
        if (target == null) return;

        // Sprint-reset attack
        mc.player.setSprinting(false);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.setSprinting(true);
        attackCount++;

        // Strafe after the configured number of attacks
        if (strafe.isEnabled() && attackCount >= attacksPerCycle.get()) {
            attackCount = 0;
            Vec3d look  = mc.player.getRotationVec(1.0f);
            Vec3d perp  = new Vec3d(-look.z, 0, look.x).normalize().multiply(strafeSpeed.get() * strafeDir);
            mc.player.addVelocity(perp.x, 0, perp.z);
            strafeDir = -strafeDir; // alternate direction
        }
    }

    private LivingEntity findNearest() {
        LivingEntity nearest = null;
        double best = range.get();
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity p)) continue;
            if (p == mc.player) continue;
            double d = mc.player.distanceTo(p);
            if (d < best) { best = d; nearest = p; }
        }
        return nearest;
    }
}
