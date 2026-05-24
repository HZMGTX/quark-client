package com.ghostclient.module.modules.combat;

import com.ghostclient.GhostClient;
import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.DoubleSetting;
import com.ghostclient.setting.EnumSetting;
import com.ghostclient.setting.IntSetting;
import com.ghostclient.util.EntityUtil;
import com.ghostclient.util.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * KillAura - automatically attacks nearby entities every configurable delay.
 */
public class KillAura extends Module {

    public enum SortMode {
        DISTANCE, HEALTH, ANGLE
    }

    // Settings
    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 3.0, 2.0, 6.0));

    private final BoolSetting rotations = register(new BoolSetting(
            "Rotations", "Rotate toward target before attacking", true));

    private final BoolSetting throughWalls = register(new BoolSetting(
            "Through Walls", "Attack through solid blocks", false));

    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "Only Players", "Only target other players", false));

    private final IntSetting attackDelay = register(new IntSetting(
            "Attack Delay", "Ticks between attacks (0 = every tick)", 10, 0, 20));

    private final EnumSetting<SortMode> sortMode = register(new EnumSetting<>(
            "Sort Mode", "How to prioritize targets", SortMode.DISTANCE));

    private final IntSetting maxTargets = register(new IntSetting(
            "Max Targets", "Maximum number of entities to attack per swing", 1, 1, 5));

    private final BoolSetting swing = register(new BoolSetting(
            "Swing", "Play the hand-swing animation", true));

    // State
    private int ticksSinceLastAttack = 0;

    public KillAura() {
        super("KillAura", "Automatically attacks nearby entities", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        ticksSinceLastAttack = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        ticksSinceLastAttack++;
        if (ticksSinceLastAttack < attackDelay.get()) return;

        // Collect valid targets
        List<LivingEntity> targets = new ArrayList<>();
        double r = range.get();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;

            // Only players filter
            if (onlyPlayers.isEnabled() && !(entity instanceof PlayerEntity)) continue;

            // Friend check
            if (entity instanceof PlayerEntity player) {
                String name = player.getGameProfile().getName();
                if (GhostClient.getInstance().getFriendManager().isFriend(name)) continue;
            }

            // Range check
            if (EntityUtil.distanceTo(entity) > r) continue;

            // Line-of-sight check (unless throughWalls is enabled)
            if (!throughWalls.isEnabled()) {
                if (mc.world.raycastBlock(
                        mc.player.getEyePos(),
                        entity.getEyePos(),
                        mc.player.getBlockPos(),
                        entity.getBoundingBox(),
                        entity) != null) {
                    // Simple visibility approximation: if there's a block between eyes, skip
                    // Use basic distance-based LOS from world
                    if (!mc.player.canSee(entity)) continue;
                }
            }

            targets.add(living);
        }

        if (targets.isEmpty()) return;

        // Sort
        switch (sortMode.get()) {
            case DISTANCE -> targets.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
            case HEALTH   -> targets.sort(Comparator.comparingDouble(LivingEntity::getHealth));
            case ANGLE    -> targets.sort(Comparator.comparingDouble(e -> RotationUtil.getAngleTo(e)));
        }

        int count = Math.min(maxTargets.get(), targets.size());

        for (int i = 0; i < count; i++) {
            LivingEntity target = targets.get(i);

            // Rotate to target
            if (rotations.isEnabled()) {
                Vec3d eyePos = target.getEyePos();
                float yaw   = RotationUtil.getYaw(eyePos);
                float pitch = RotationUtil.getPitch(eyePos);
                mc.player.setYaw(yaw);
                mc.player.setPitch(MathHelper.clamp(pitch, -90f, 90f));
            }

            // Attack
            mc.interactionManager.attackEntity(mc.player, target);

            if (swing.isEnabled()) {
                mc.player.swingMainHand();
            }
        }

        ticksSinceLastAttack = 0;
    }
}
