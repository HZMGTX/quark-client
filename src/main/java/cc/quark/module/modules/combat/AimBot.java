package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * AimBot - Automatically aims at the nearest player within the configured FOV.
 * Smoothly rotates towards the target's head position each tick.
 */
public class AimBot extends Module {

    private final DoubleSetting fov = register(new DoubleSetting(
            "FOV", "Field of view in degrees to search for targets", 180.0, 1.0, 360.0));

    private final DoubleSetting smooth = register(new DoubleSetting(
            "Smooth", "Rotation smoothing factor (higher = slower turning)", 3.0, 1.0, 20.0));

    private final BoolSetting throughWalls = register(new BoolSetting(
            "Through Walls", "Aim at targets behind walls", false));

    private final BoolSetting onlyWhenHoldingSword = register(new BoolSetting(
            "Only When Holding Sword", "Only aim when holding a sword or axe", false));

    public AimBot() {
        super("AimBot", "Automatically aims at the nearest player in FOV", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // Check weapon requirement
        if (onlyWhenHoldingSword.isEnabled()) {
            var mainHandItem = mc.player.getMainHandStack().getItem();
            if (!(mainHandItem instanceof SwordItem) && !(mainHandItem instanceof AxeItem)) return;
        }

        PlayerEntity target = findBestTarget();
        if (target == null) return;

        // Calculate angles to target's eye position
        Vec3d eyePos = target.getEyePos();
        Vec3d myEyes = mc.player.getEyePos();

        double dx = eyePos.x - myEyes.x;
        double dy = eyePos.y - myEyes.y;
        double dz = eyePos.z - myEyes.z;

        float targetYaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = (float) Math.toDegrees(Math.atan2(-dy, Math.sqrt(dx * dx + dz * dz)));
        targetPitch = MathHelper.clamp(targetPitch, -90f, 90f);

        // Smooth rotation
        float currentYaw   = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        float yawDiff   = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;

        float smoothFactor = (float) smooth.get();
        float newYaw   = currentYaw   + yawDiff   / smoothFactor;
        float newPitch = currentPitch + pitchDiff / smoothFactor;

        mc.player.setYaw(newYaw);
        mc.player.setPitch(MathHelper.clamp(newPitch, -90f, 90f));
    }

    private PlayerEntity findBestTarget() {
        if (mc.world == null || mc.player == null) return null;

        PlayerEntity best = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player == mc.player) continue;
            if (player.isRemoved() || player.getHealth() <= 0f) continue;

            // FOV check
            Vec3d myEyes = mc.player.getEyePos();
            Vec3d targetEyes = player.getEyePos();
            double dx = targetEyes.x - myEyes.x;
            double dy = targetEyes.y - myEyes.y;
            double dz = targetEyes.z - myEyes.z;

            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            // Calculate angle to target from player's look direction
            float targetYaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float targetPitch = (float) Math.toDegrees(Math.atan2(-dy, Math.sqrt(dx * dx + dz * dz)));

            float yawDiff   = Math.abs(MathHelper.wrapDegrees(targetYaw - mc.player.getYaw()));
            float pitchDiff = Math.abs(targetPitch - mc.player.getPitch());
            float angleDiff = (float) Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);

            if (angleDiff > fov.get() / 2.0f) continue;

            // Line-of-sight check
            if (!throughWalls.isEnabled() && !mc.player.canSee(player)) continue;

            if (dist < bestDist) {
                bestDist = dist;
                best = player;
            }
        }

        return best;
    }
}
