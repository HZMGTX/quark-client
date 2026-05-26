package cc.quark.module.modules.combat;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.EnumSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.EntityUtil;
import cc.quark.util.RotationUtil;
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
            
    private final IntSetting turnSpeed = register(new IntSetting(
            "Turn Speed", "Max degrees to turn per tick (smooths aim)", 45, 10, 180));

    private final BoolSetting throughWalls = register(new BoolSetting(
            "Through Walls", "Attack through solid blocks", false));

    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "Only Players", "Only target other players", false));

    private final DoubleSetting cooldownPct = register(new DoubleSetting(
            "Cooldown %", "How full the 1.9+ attack bar must be", 100.0, 10.0, 100.0));

    private final EnumSetting<SortMode> sortMode = register(new EnumSetting<>(
            "Sort Mode", "How to prioritize targets", SortMode.DISTANCE));

    private final IntSetting maxTargets = register(new IntSetting(
            "Max Targets", "Maximum number of entities to attack per swing", 1, 1, 5));

    private final BoolSetting swing = register(new BoolSetting(
            "Swing", "Play the hand-swing animation", true));

    // State
    private int ticksSinceLastAttack = 0;

    private LivingEntity currentTarget = null;

    public KillAura() {
        super("KillAura", "Automatically attacks nearby entities", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        ticksSinceLastAttack = 0;
        currentTarget = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // 1.9+ Cooldown check instead of fixed ticks
        if (mc.player.getAttackCooldownProgress(0.0f) < (cooldownPct.get() / 100.0)) return;

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
                if (Quark.getInstance().getFriendManager().isFriend(name)) continue;
            }

            // Range check
            if (EntityUtil.distanceTo(entity) > r) continue;

            // Line-of-sight check (unless throughWalls is enabled)
            if (!throughWalls.isEnabled()) {
                if (!mc.player.canSee(entity)) continue;
            }

            targets.add(living);
        }

        if (targets.isEmpty()) {
            currentTarget = null;
            return;
        }

        // Sort
        switch (sortMode.get()) {
            case DISTANCE -> targets.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
            case HEALTH   -> targets.sort(Comparator.comparingDouble(LivingEntity::getHealth));
            case ANGLE    -> targets.sort(Comparator.comparingDouble(e -> RotationUtil.getAngleTo(e)));
        }

        int count = Math.min(maxTargets.get(), targets.size());
        
        if (count > 0) {
            currentTarget = targets.get(0);
        }

        for (int i = 0; i < count; i++) {
            LivingEntity target = targets.get(i);

            // Rotate to target (Smooth)
            if (rotations.isEnabled()) {
                Vec3d eyePos = target.getEyePos();
                float targetYaw   = RotationUtil.getYaw(eyePos);
                float targetPitch = RotationUtil.getPitch(eyePos);
                
                float maxTurn = turnSpeed.get();
                float currentYaw = mc.player.getYaw();
                float currentPitch = mc.player.getPitch();
                
                float yawDiff = MathHelper.wrapDegrees(targetYaw - currentYaw);
                float pitchDiff = MathHelper.wrapDegrees(targetPitch - currentPitch);
                
                if (yawDiff > maxTurn) yawDiff = maxTurn;
                if (yawDiff < -maxTurn) yawDiff = -maxTurn;
                if (pitchDiff > maxTurn) pitchDiff = maxTurn;
                if (pitchDiff < -maxTurn) pitchDiff = -maxTurn;
                
                mc.player.setYaw(currentYaw + yawDiff);
                mc.player.setPitch(MathHelper.clamp(currentPitch + pitchDiff, -90f, 90f));
                
                // Don't attack if we haven't finished turning to face them (within 10 degrees)
                if (Math.abs(MathHelper.wrapDegrees(targetYaw - mc.player.getYaw())) > 10.0f) {
                    continue; // Skip attack this tick, just keep turning
                }
            }

            // Attack
            mc.interactionManager.attackEntity(mc.player, target);

            if (swing.isEnabled()) {
                mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
            }
        }
    }
    
    public Entity getTarget() {
        return currentTarget;
    }
}
