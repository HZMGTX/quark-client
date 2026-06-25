package cc.quark.module.modules.combat;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.EntityUtil;
import cc.quark.util.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * KillAura2 - an improved KillAura with priority modes, FOV filter, multi-target,
 * and configurable attack delay.
 */
public class KillAura2 extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 3.5, 1.0, 6.0));

    private final ModeSetting priority = register(new ModeSetting(
            "Priority", "Target selection priority",
            "Closest", "Closest", "LowestHP", "HighestHP", "ByAngle"));

    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "Only Players", "Only target other players", true));

    private final BoolSetting targetMobs = register(new BoolSetting(
            "Mobs", "Also attack hostile mobs", false));

    private final BoolSetting fovFilter = register(new BoolSetting(
            "FOV Filter", "Only attack targets within FOV", true));

    private final DoubleSetting fov = register(new DoubleSetting(
            "FOV", "FOV half-angle for target detection (degrees)", 90.0, 10.0, 360.0));

    private final IntSetting maxTargets = register(new IntSetting(
            "Max Targets", "Maximum targets hit per swing (multi-aura)", 1, 1, 8));

    private final DoubleSetting cooldownPct = register(new DoubleSetting(
            "Cooldown %", "Minimum attack cooldown required (0–100)", 100.0, 10.0, 100.0));

    private final IntSetting attackDelay = register(new IntSetting(
            "Attack Delay ms", "Extra delay in ms between attacks (0 = none)", 0, 0, 500));

    private final BoolSetting rotations = register(new BoolSetting(
            "Rotations", "Rotate toward target before attacking", true));

    private final IntSetting turnSpeed = register(new IntSetting(
            "Turn Speed", "Max degrees to rotate per tick", 45, 5, 180));

    private final BoolSetting swing = register(new BoolSetting(
            "Swing", "Play hand-swing animation on attack", true));

    private long lastAttackMs = 0L;

    public KillAura2() {
        super("KillAura2", "Improved KillAura with priority, multi-target and FOV settings", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastAttackMs = 0L;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Cooldown check
        if (mc.player.getAttackCooldownProgress(0.0f) < (cooldownPct.get() / 100.0)) return;

        // Attack delay check
        if (attackDelay.get() > 0 && System.currentTimeMillis() - lastAttackMs < attackDelay.get()) return;

        List<LivingEntity> targets = collectTargets();
        if (targets.isEmpty()) return;

        // Sort by priority mode
        switch (priority.get()) {
            case "Closest"   -> targets.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
            case "LowestHP"  -> targets.sort(Comparator.comparingDouble(LivingEntity::getHealth));
            case "HighestHP" -> targets.sort(Comparator.comparingDouble((LivingEntity e) -> e.getHealth()).reversed());
            case "ByAngle"   -> targets.sort(Comparator.comparingDouble(e -> RotationUtil.getAngleTo(e)));
        }

        int count = Math.min(maxTargets.get(), targets.size());
        LivingEntity primary = targets.get(0);

        // Rotate toward primary target
        if (rotations.isEnabled()) {
            float desiredYaw   = RotationUtil.getYaw(primary.getEyePos());
            float desiredPitch = RotationUtil.getPitch(primary.getEyePos());

            float newYaw   = RotationUtil.smoothYaw(mc.player.getYaw(), desiredYaw, turnSpeed.get());
            float newPitch = RotationUtil.smoothPitch(mc.player.getPitch(), desiredPitch, turnSpeed.get());

            mc.player.setYaw(newYaw);
            mc.player.setPitch(newPitch);
            mc.player.headYaw = newYaw;

            // Abort if not close enough to target yet
            float yawErr = Math.abs(MathHelper.wrapDegrees(desiredYaw - newYaw));
            if (yawErr > 15f) return;
        }

        // Attack up to maxTargets
        for (int i = 0; i < count; i++) {
            LivingEntity target = targets.get(i);
            mc.interactionManager.attackEntity(mc.player, target);
            if (swing.isEnabled()) mc.player.swingHand(Hand.MAIN_HAND);
        }
        lastAttackMs = System.currentTimeMillis();
    }

    private List<LivingEntity> collectTargets() {
        List<LivingEntity> result = new ArrayList<>();
        double r = range.get();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;

            boolean isPlayer = entity instanceof PlayerEntity;
            boolean isMob    = EntityUtil.isMob(entity);

            if (onlyPlayers.isEnabled()) {
                if (!isPlayer) continue;
            } else {
                if (isMob && !targetMobs.isEnabled()) continue;
                if (!isPlayer && !isMob) continue;
            }

            // Friend check
            if (isPlayer) {
                String name = ((PlayerEntity) entity).getGameProfile().getName();
                if (Quark.getInstance().getFriendManager().isFriend(name)) continue;
            }

            // AntiBot filter
            if (AntiBot.isBot(entity)) continue;

            // Range
            if (EntityUtil.distanceTo(entity) > r) continue;

            // FOV
            if (fovFilter.isEnabled()) {
                if (RotationUtil.getAngleTo(entity) > fov.get() / 2.0) continue;
            }

            result.add(living);
        }
        return result;
    }
}
