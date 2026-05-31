package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BowPredict extends Module {

    private final DoubleSetting leadAmount = register(new DoubleSetting(
            "Lead Amount", "How many ticks ahead to lead the target", 5.0, 0.0, 20.0));

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Maximum target range in blocks", 30.0, 5.0, 80.0));

    public BowPredict() {
        super("BowPredict", "Leads bow aim ahead of moving targets based on velocity", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof BowItem)) return;
        if (!mc.player.isUsingItem()) return;

        LivingEntity target = findNearestTarget();
        if (target == null) return;

        Vec3d vel = target.getVelocity();
        double lead = leadAmount.get();

        Vec3d predictedPos = target.getEyePos().add(vel.multiply(lead));
        Vec3d eyePos = mc.player.getEyePos();

        double dx = predictedPos.x - eyePos.x;
        double dy = predictedPos.y - eyePos.y;
        double dz = predictedPos.z - eyePos.z;

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(Math.atan2(-dy, Math.sqrt(dx * dx + dz * dz)));
        pitch = MathHelper.clamp(pitch, -90f, 90f);

        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }

    private LivingEntity findNearestTarget() {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;
            if (dist < bestDist) {
                bestDist = dist;
                best = living;
            }
        }
        return best;
    }
}
