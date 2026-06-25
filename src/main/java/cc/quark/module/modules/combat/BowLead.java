package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BowLead extends Module {

    private final DoubleSetting range   = register(new DoubleSetting("Range",   "Max target range",              30.0, 5.0, 60.0));
    private final DoubleSetting predict = register(new DoubleSetting("Predict", "Lead multiplier (ticks ahead)", 0.5,  0.0, 5.0));

    public BowLead() {
        super("BowLead", "Predicts movement for bow shots", Category.COMBAT);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null || mc.world == null) return;

        // Only active when holding a bow and charging
        if (!(mc.player.getMainHandStack().getItem() instanceof BowItem)) return;
        if (!mc.player.isUsingItem()) return;

        LivingEntity target = findNearest();
        if (target == null) return;

        Vec3d myEyes    = mc.player.getEyePos();
        Vec3d targetPos = target.getPos().add(0, target.getHeight() * 0.5, 0);

        // Lead based on target velocity
        double ticksAhead = predict.get();
        Vec3d  velocity   = target.getVelocity();
        Vec3d  predicted  = targetPos.add(velocity.multiply(ticksAhead));

        double dx = predicted.x - myEyes.x;
        double dy = predicted.y - myEyes.y;
        double dz = predicted.z - myEyes.z;

        // Account for arrow drop (gravity ~ 0.03 per tick^2, approximate)
        double dist  = Math.sqrt(dx * dx + dz * dz);
        double power = 3.0; // blocks/tick approximate arrow speed
        double ticks = dist / power;
        dy += 0.5 * 0.03 * ticks * ticks; // compensate for gravity

        float calcYaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float calcPitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        calcPitch = MathHelper.clamp(calcPitch, -90f, 90f);

        event.setYaw(calcYaw);
        event.setPitch(calcPitch);
    }

    private LivingEntity findNearest() {
        LivingEntity best = null;
        double bestDist   = range.get();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity)) continue;
            double d = mc.player.distanceTo(e);
            if (d < bestDist) {
                bestDist = d;
                best     = (LivingEntity) e;
            }
        }
        return best;
    }
}
