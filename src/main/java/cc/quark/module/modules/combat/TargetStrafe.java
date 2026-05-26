package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class TargetStrafe extends Module {

    private final DoubleSetting radius = register(new DoubleSetting("Radius", "Strafe radius around target", 2.5, 1.0, 5.0));
    private final BoolSetting autoSprint = register(new BoolSetting("Auto Sprint", "Sprint while strafing", true));
    private float strafeAngle = 0f;

    public TargetStrafe() {
        super("TargetStrafe", "Strafes around the nearest target automatically", Category.COMBAT);
    }

    @Override
    public void onEnable() { strafeAngle = 0f; }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        LivingEntity target = findNearestEntity();
        if (target == null) return;

        double tx = target.getX();
        double tz = target.getZ();

        double dx = mc.player.getX() - tx;
        double dz = mc.player.getZ() - tz;
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx));
        mc.player.setYaw(yaw + 90f);

        strafeAngle += 3f;
        double rad = Math.toRadians(strafeAngle);
        double r = radius.get();
        double nx = tx + Math.cos(rad) * r;
        double nz = tz + Math.sin(rad) * r;
        double moveX = nx - mc.player.getX();
        double moveZ = nz - mc.player.getZ();
        double len = Math.sqrt(moveX * moveX + moveZ * moveZ);
        if (len > 0) {
            mc.player.addVelocity(moveX / len * 0.2, 0, moveZ / len * 0.2);
        }
        if (autoSprint.isEnabled()) mc.player.setSprinting(true);
    }

    private LivingEntity findNearestEntity() {
        LivingEntity nearest = null;
        double best = 6.0;
        for (var e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le)) continue;
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity)) continue;
            double d = mc.player.distanceTo(le);
            if (d < best) { best = d; nearest = le; }
        }
        return nearest;
    }
}
