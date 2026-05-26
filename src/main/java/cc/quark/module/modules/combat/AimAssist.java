package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.EntityUtil;
import cc.quark.util.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AimAssist extends Module {

    private final DoubleSetting fov = register(new DoubleSetting(
            "FOV", "Half-angle of the FOV cone for target detection (degrees)", 90.0, 30.0, 180.0));

    private final DoubleSetting strength = register(new DoubleSetting(
            "Strength", "Lerp factor per tick toward target (0.1 = gentle, 1.0 = snap)", 0.3, 0.1, 1.0));

    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "Only Players", "Only assist against other players", false));

    private final BoolSetting visibleOnly = register(new BoolSetting(
            "Visible Only", "Only assist toward targets with line-of-sight", false));

    private final BoolSetting requireAttack = register(new BoolSetting(
            "Only On Click", "Only assist while holding left mouse button", false));

    private final BoolSetting targetMobs = register(new BoolSetting(
            "Mobs", "Target hostile mobs", true));

    private final BoolSetting targetAnimals = register(new BoolSetting(
            "Animals", "Target passive animals", false));

    public AimAssist() {
        super("AimAssist", "Smoothly nudges aim toward the nearest valid target within FOV", Category.COMBAT);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null || mc.world == null) return;

        if (requireAttack.isEnabled()) {
            if (mc.options == null || !mc.options.attackKey.isPressed()) return;
        }

        LivingEntity target = findBestTarget();
        if (target == null) return;

        Vec3d targetEye = target.getEyePos();
        float desiredYaw   = RotationUtil.getYaw(targetEye);
        float desiredPitch = RotationUtil.getPitch(targetEye);

        float lerp = (float) strength.get();

        float currentYaw   = event.getYaw();
        float currentPitch = event.getPitch();

        float yawDiff   = MathHelper.wrapDegrees(desiredYaw - currentYaw);
        float pitchDiff = desiredPitch - currentPitch;

        float newYaw   = currentYaw   + yawDiff   * lerp;
        float newPitch = currentPitch + pitchDiff * lerp;

        event.setYaw(newYaw);
        event.setPitch(MathHelper.clamp(newPitch, -90f, 90f));

        mc.player.setYaw(newYaw);
        mc.player.setPitch(MathHelper.clamp(newPitch, -90f, 90f));
        mc.player.headYaw = newYaw;
        mc.player.bodyYaw = newYaw;
    }

    private LivingEntity findBestTarget() {
        List<LivingEntity> candidates = new ArrayList<>();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;

            boolean isPlayer = entity instanceof PlayerEntity;
            boolean isAnimal = entity instanceof AnimalEntity;
            boolean isMob    = entity instanceof MobEntity && !isAnimal;

            if (onlyPlayers.isEnabled()) {
                if (!isPlayer) continue;
            } else {
                if (isAnimal && !targetAnimals.isEnabled()) continue;
                if (isMob    && !targetMobs.isEnabled()) continue;
                if (!isPlayer && !isAnimal && !isMob) continue;
            }

            float angle = RotationUtil.getAngleTo(entity);
            if (angle > fov.get() / 2.0) continue;

            if (visibleOnly.isEnabled() && !EntityUtil.hasLineOfSight(mc.player, entity)) continue;

            candidates.add(living);
        }

        if (candidates.isEmpty()) return null;
        candidates.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
        return candidates.get(0);
    }
}
