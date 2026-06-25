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
import net.minecraft.util.math.Vec3d;

public class AxisAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 4.0, 2.0, 6.0));

    private final BoolSetting horizontal = register(new BoolSetting(
            "Horizontal", "Only attack targets on the same X or Z axis", true));

    private final BoolSetting vertical = register(new BoolSetting(
            "Vertical", "Only attack targets on the same Y axis", false));

    private final DoubleSetting axisTolerance = register(new DoubleSetting(
            "Tolerance", "Max offset from axis in blocks to still count as aligned", 0.8, 0.1, 3.0));

    public AxisAura() {
        super("AxisAura", "Locks KillAura attacks to only targets aligned on a specific axis", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0f) < 1f) return;

        Vec3d myPos = mc.player.getPos();
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;
            if (!(entity instanceof PlayerEntity)) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;

            Vec3d ePos = entity.getPos();
            boolean aligned = false;

            if (horizontal.isEnabled()) {
                double xOff = Math.abs(ePos.x - myPos.x);
                double zOff = Math.abs(ePos.z - myPos.z);
                if (xOff <= axisTolerance.get() || zOff <= axisTolerance.get()) {
                    aligned = true;
                }
            }

            if (vertical.isEnabled()) {
                double yOff = Math.abs(ePos.y - myPos.y);
                if (yOff <= axisTolerance.get()) {
                    aligned = true;
                }
            }

            if (!aligned) continue;
            if (dist < bestDist) {
                bestDist = dist;
                best = living;
            }
        }

        if (best == null) return;

        mc.interactionManager.attackEntity(mc.player, best);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
