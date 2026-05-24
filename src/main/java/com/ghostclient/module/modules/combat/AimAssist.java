package com.ghostclient.module.modules.combat;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventPreMotion;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.DoubleSetting;
import com.ghostclient.util.EntityUtil;
import com.ghostclient.util.RotationUtil;
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

/**
 * AimAssist - smoothly rotates toward the nearest valid target within a configurable FOV.
 */
public class AimAssist extends Module {

    private final DoubleSetting fov = register(new DoubleSetting(
            "FOV", "Field of view cone for target detection (degrees)", 90.0, 30.0, 180.0));

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Degrees per tick to rotate toward target", 3.0, 1.0, 10.0));

    private final BoolSetting onlyOnClick = register(new BoolSetting(
            "Only On Click", "Only assist while holding left mouse button", false));

    private final BoolSetting targetPlayers = register(new BoolSetting(
            "Players", "Target other players", true));

    private final BoolSetting targetMobs = register(new BoolSetting(
            "Mobs", "Target hostile mobs", true));

    private final BoolSetting targetAnimals = register(new BoolSetting(
            "Animals", "Target passive animals", false));

    public AimAssist() {
        super("AimAssist", "Smoothly rotates toward the nearest target within FOV", Category.COMBAT);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null || mc.world == null) return;

        // If onlyOnClick is active, check mouse button 0 (left click)
        if (onlyOnClick.isEnabled()) {
            if (mc.mouse == null || !mc.options.attackKey.isPressed()) return;
        }

        LivingEntity closest = findBestTarget();
        if (closest == null) return;

        Vec3d eyePos = closest.getEyePos();
        float desiredYaw   = RotationUtil.getYaw(eyePos);
        float desiredPitch = RotationUtil.getPitch(eyePos);

        float smoothFactor = (float) speed.get();

        float newYaw   = RotationUtil.smoothYaw(event.getYaw(), desiredYaw, smoothFactor);
        float newPitch = RotationUtil.smoothPitch(event.getPitch(), desiredPitch, smoothFactor);

        event.setYaw(newYaw);
        event.setPitch(MathHelper.clamp(newPitch, -90f, 90f));

        // Also update visual rotation so it matches packet rotation
        mc.player.setYaw(newYaw);
        mc.player.setPitch(MathHelper.clamp(newPitch, -90f, 90f));
    }

    /**
     * Finds the nearest living entity within the configured FOV, filtering by type.
     */
    private LivingEntity findBestTarget() {
        List<LivingEntity> candidates = new ArrayList<>();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;

            boolean isPlayer = entity instanceof PlayerEntity;
            boolean isAnimal = entity instanceof AnimalEntity;
            boolean isMob    = entity instanceof MobEntity && !isAnimal;

            if (isPlayer  && !targetPlayers.isEnabled()) continue;
            if (isAnimal  && !targetAnimals.isEnabled()) continue;
            if (isMob     && !targetMobs.isEnabled()) continue;
            // Skip entities that are none of the three types if all are filtered
            if (!isPlayer && !isAnimal && !isMob) continue;

            // FOV check
            float angle = RotationUtil.getAngleTo(entity);
            if (angle > fov.get() / 2.0) continue;

            candidates.add(living);
        }

        if (candidates.isEmpty()) return null;

        candidates.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
        return candidates.get(0);
    }
}
