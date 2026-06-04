package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class TargetStrafe2 extends Module {

    private final DoubleSetting range  = register(new DoubleSetting("Range",  "Target acquisition range",     3.5, 1.0, 8.0));
    private final DoubleSetting radius = register(new DoubleSetting("Radius", "Strafe circle radius (blocks)", 2.0, 0.5, 6.0));

    private double angle = 0.0;
    private int    direction = 1; // 1 = clockwise, -1 = counter-clockwise

    public TargetStrafe2() {
        super("TargetStrafe2", "Enhanced target strafe with circle movement", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        angle = 0.0;
        direction = 1;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        LivingEntity target = findNearestPlayer();
        if (target == null) return;

        // Advance angle
        angle += direction * 4.5; // degrees per tick
        if (angle >= 360) angle -= 360;
        if (angle < 0)    angle += 360;

        // Calculate ideal position on circle around target
        double rad = Math.toRadians(angle);
        double r   = radius.get();
        double tx  = target.getX() + r * Math.cos(rad);
        double tz  = target.getZ() + r * Math.sin(rad);

        // Apply velocity toward that circle point
        Vec3d vel = mc.player.getVelocity();
        double dx = tx - mc.player.getX();
        double dz = tz - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist > 0.01) {
            double speed = 0.26;
            mc.player.setVelocity(dx / dist * speed, vel.y, dz / dist * speed);
        }

        // Flip direction randomly occasionally to avoid patterns
        if (Math.random() < 0.005) direction = -direction;
    }

    private LivingEntity findNearestPlayer() {
        LivingEntity nearest = null;
        double bestDist = range.get();
        if (mc.world == null) return null;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity player)) continue;
            double d = mc.player.distanceTo(player);
            if (d < bestDist) {
                bestDist = d;
                nearest  = player;
            }
        }
        return nearest;
    }
}
