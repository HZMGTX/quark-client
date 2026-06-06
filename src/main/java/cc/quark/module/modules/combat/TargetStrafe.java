package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class TargetStrafe extends Module {

    private final DoubleSetting radius = register(new DoubleSetting(
            "Radius", "Circle-strafe radius around target", 3.0, 2.0, 5.0));

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Strafe movement speed", 2.0, 1.0, 4.0));

    private int direction = 1;
    private int directionTimer = 0;
    private double angle = 0.0;

    public TargetStrafe() {
        super("TargetStrafe", "Strafes in a circle around the nearest target while attacking", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        directionTimer++;
        if (directionTimer > 60) {
            direction = -direction;
            directionTimer = 0;
        }

        double r = radius.get();
        double s = speed.get() * 0.05;
        angle += s * direction;

        double tx = target.getX() + Math.cos(angle) * r;
        double tz = target.getZ() + Math.sin(angle) * r;

        double dx = tx - mc.player.getX();
        double dz = tz - mc.player.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len > 0.01) {
            double moveSpeed = speed.get() * 0.1;
            mc.player.setVelocity(
                    (dx / len) * moveSpeed,
                    mc.player.getVelocity().y,
                    (dz / len) * moveSpeed
            );
        }

        double yaw = Math.toDegrees(Math.atan2(
                target.getZ() - mc.player.getZ(),
                target.getX() - mc.player.getX())) - 90.0;
        mc.player.setYaw((float) yaw);
    }

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (var ent : mc.world.getEntities()) {
            if (!(ent instanceof LivingEntity le)) continue;
            if (ent == mc.player) continue;
            if (!(le instanceof PlayerEntity)) continue;
            if (le.isRemoved() || le.getHealth() <= 0f) continue;
            double d = mc.player.distanceTo(le);
            if (d < bestDist) {
                bestDist = d;
                best = le;
            }
        }
        return best;
    }
}
