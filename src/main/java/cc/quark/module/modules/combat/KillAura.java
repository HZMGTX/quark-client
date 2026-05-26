package cc.quark.module.modules.combat;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.ghost.GhostManager;
import cc.quark.ghost.RotationManager;
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

public class KillAura extends Module {

    public enum SortMode {
        DISTANCE, HEALTH, ANGLE
    }

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

    private final BoolSetting attackMobs = register(new BoolSetting(
            "Attack Mobs", "Also target hostile mobs (not just players)", false));

    private final DoubleSetting cooldownPct = register(new DoubleSetting(
            "Cooldown %", "How full the 1.9+ attack bar must be", 100.0, 10.0, 100.0));

    private final EnumSetting<SortMode> sortMode = register(new EnumSetting<>(
            "Sort Mode", "How to prioritize targets", SortMode.DISTANCE));

    private final IntSetting maxTargets = register(new IntSetting(
            "Max Targets", "Maximum number of entities to attack per swing", 1, 1, 5));

    private final BoolSetting swing = register(new BoolSetting(
            "Swing", "Play the hand-swing animation", true));

    private final BoolSetting wTap = register(new BoolSetting(
            "W-Tap", "Release forward key briefly on attack to reset sprint and boost knockback", true));

    private final BoolSetting sprintReset = register(new BoolSetting(
            "Sprint Reset", "Reset sprint momentum on each attack", false));

    private final BoolSetting fovFilter = register(new BoolSetting(
            "FOV Filter", "Only attack targets within the configured FOV", false));

    private final DoubleSetting fov = register(new DoubleSetting(
            "FOV", "FOV degrees to consider targets in (from your look direction)", 90.0, 10.0, 360.0));

    private final BoolSetting ghostMode = register(new BoolSetting(
            "Ghost Mode", "Integrate GhostManager limits and RotationManager for silent aim", true));

    private int ticksSinceLastAttack = 0;
    private long lastAttackMs = 0L;
    private int wTapTicks = 0;

    private LivingEntity currentTarget = null;

    public KillAura() {
        super("KillAura", "Automatically attacks nearby entities", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        ticksSinceLastAttack = 0;
        lastAttackMs = 0L;
        currentTarget = null;
        wTapTicks = 0;
    }

    @Override
    public void onDisable() {
        if (mc.player != null && wTapTicks > 0) {
            mc.options.forwardKey.setPressed(false);
            wTapTicks = 0;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Re-enable forward key after W-tap pause
        if (wTapTicks > 0) {
            wTapTicks--;
            if (wTapTicks == 0) {
                // restore forward key state based on actual input
                mc.options.forwardKey.setPressed(mc.player.input.movementForward > 0);
            }
            return;
        }

        // 1.9+ Cooldown check
        if (mc.player.getAttackCooldownProgress(0.0f) < (cooldownPct.get() / 100.0)) return;

        // Ghost Mode: enforce minimum attack delay
        if (ghostMode.isEnabled()) {
            long minDelay = GhostManager.INSTANCE.getMinAttackDelay();
            if (minDelay > 0 && System.currentTimeMillis() - lastAttackMs < minDelay) return;
        }

        // Determine effective reach cap
        double effectiveRange = range.get();
        if (ghostMode.isEnabled()) {
            double safeReach = GhostManager.INSTANCE.getMaxReach();
            effectiveRange = Math.min(effectiveRange, safeReach);
        }

        // Collect valid targets
        List<LivingEntity> targets = new ArrayList<>();
        double r = effectiveRange;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;

            // Player/mob filter logic
            boolean isPlayer = entity instanceof PlayerEntity;
            boolean isMob = EntityUtil.isMob(entity);

            if (onlyPlayers.isEnabled()) {
                if (!isPlayer) continue;
            } else if (!attackMobs.isEnabled() && !isPlayer) {
                continue;
            }

            // Friend check
            if (isPlayer) {
                String name = ((PlayerEntity) entity).getGameProfile().getName();
                if (Quark.getInstance().getFriendManager().isFriend(name)) continue;
            }

            // FOV filter: check angle between player look and direction to entity
            if (fovFilter.isEnabled()) {
                float angleTo = RotationUtil.getAngleTo(entity);
                if (angleTo > fov.get() / 2.0f) continue;
            }

            // Range check using bounding box expansion for better hit detection
            double entityHalfWidth = entity.getWidth() / 2.0 + 0.1;
            double distToCenter = EntityUtil.distanceTo(entity);
            if (distToCenter - entityHalfWidth > r) continue;

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

        // Ghost Mode: strafe attack guard
        if (ghostMode.isEnabled() && !GhostManager.INSTANCE.canStrafeAttack()) {
            if (mc.player.sidewaysSpeed != 0f) return;
        }

        for (int i = 0; i < count; i++) {
            LivingEntity target = targets.get(i);

            Vec3d eyePos = target.getEyePos();
            Vec3d myEyes = mc.player.getEyePos();
            double dx = eyePos.x - myEyes.x;
            double dy = eyePos.y - myEyes.y;
            double dz = eyePos.z - myEyes.z;

            float calcYaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float calcPitch = (float) Math.toDegrees(Math.atan2(-dy, Math.sqrt(dx * dx + dz * dz)));
            calcPitch = MathHelper.clamp(calcPitch, -90f, 90f);

            if (rotations.isEnabled()) {
                if (ghostMode.isEnabled()) {
                    RotationManager.INSTANCE.requestRotation(calcYaw, calcPitch, 10, true);

                    float serverYaw   = RotationManager.INSTANCE.getServerYaw();
                    float serverPitch = RotationManager.INSTANCE.getServerPitch();
                    float yawErr      = Math.abs(MathHelper.wrapDegrees(calcYaw - serverYaw));
                    float pitchErr    = Math.abs(calcPitch - serverPitch);
                    if (yawErr > 10f || pitchErr > 10f) continue;
                } else {
                    float maxTurn    = turnSpeed.get();
                    float currentYaw = mc.player.getYaw();
                    float currentPitch = mc.player.getPitch();

                    float yawDiff   = MathHelper.wrapDegrees(calcYaw - currentYaw);
                    float pitchDiff = MathHelper.wrapDegrees(calcPitch - currentPitch);

                    if (yawDiff   > maxTurn)  yawDiff   = maxTurn;
                    if (yawDiff   < -maxTurn) yawDiff   = -maxTurn;
                    if (pitchDiff > maxTurn)  pitchDiff = maxTurn;
                    if (pitchDiff < -maxTurn) pitchDiff = -maxTurn;

                    mc.player.setYaw(currentYaw + yawDiff);
                    mc.player.setPitch(MathHelper.clamp(currentPitch + pitchDiff, -90f, 90f));

                    if (Math.abs(MathHelper.wrapDegrees(calcYaw - mc.player.getYaw())) > 10.0f) {
                        continue;
                    }
                }
            }

            // Sprint reset before attack
            if (sprintReset.isEnabled()) {
                mc.player.setSprinting(false);
            }

            // Attack
            mc.interactionManager.attackEntity(mc.player, target);
            lastAttackMs = System.currentTimeMillis();

            if (swing.isEnabled()) {
                mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
            }

            // W-tap: release forward key for 1 tick to reset sprint and boost knockback
            if (wTap.isEnabled() && mc.player.isSprinting()) {
                mc.options.forwardKey.setPressed(false);
                mc.player.setSprinting(false);
                wTapTicks = 1;
            }

            // Re-enable sprint after reset
            if (sprintReset.isEnabled()) {
                mc.player.setSprinting(true);
            }
        }
    }

    public Entity getTarget() {
        return currentTarget;
    }
}
