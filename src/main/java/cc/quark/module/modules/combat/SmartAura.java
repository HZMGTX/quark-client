package cc.quark.module.modules.combat;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.ghost.RotationManager;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.EntityUtil;
import cc.quark.util.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SmartAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 3.5, 1.0, 6.0));

    private final IntSetting cps = register(new IntSetting(
            "CPS", "Attacks per second (clicks per second)", 12, 4, 20));

    private final BoolSetting criticals = register(new BoolSetting(
            "Criticals", "Micro-jump before attacking for guaranteed crits", true));

    private final BoolSetting wTap = register(new BoolSetting(
            "WTap", "Briefly stop sprinting before each attack to reset enemy knockback", true));

    private final BoolSetting sprintReset = register(new BoolSetting(
            "Sprint Reset", "Re-enable sprinting immediately after each attack", true));

    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "Only Players", "Only target other players", false));

    private int ticksUntilAttack = 0;

    public SmartAura() {
        super("SmartAura", "KillAura + Crits + WTap combined into one intelligent combat module", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        ticksUntilAttack = 0;
    }

    @Override
    public String getSuffix() {
        return String.format("%.1f", range.get());
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        LivingEntity target = findBestTarget();
        if (target == null) return;

        Vec3d targetEye = target.getEyePos();
        float yaw = RotationUtil.getYaw(targetEye);
        float pitch = MathHelper.clamp(RotationUtil.getPitch(targetEye), -90f, 90f);
        RotationManager.INSTANCE.requestRotation(yaw, pitch, 10, true);

        if (ticksUntilAttack > 0) {
            ticksUntilAttack--;
            return;
        }

        float attackCooldown = mc.player.getAttackCooldownProgress(0f);
        if (attackCooldown < 0.95f) return;

        if (criticals.isEnabled() && mc.player.isOnGround() && !mc.player.isSubmergedInWater()) {
            mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
        }

        if (wTap.isEnabled()) {
            mc.player.setSprinting(false);
        }

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        RotationManager.INSTANCE.notifyAttacked();

        if (sprintReset.isEnabled() || wTap.isEnabled()) {
            mc.player.setSprinting(true);
        }

        int ticksPerAttack = Math.max(1, 20 / cps.get());
        ticksUntilAttack = ticksPerAttack;
    }

    private LivingEntity findBestTarget() {
        LivingEntity best = null;
        float bestHealth = Float.MAX_VALUE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;
            if (onlyPlayers.isEnabled() && !(entity instanceof PlayerEntity)) continue;
            if (entity instanceof PlayerEntity player
                    && Quark.getInstance() != null
                    && Quark.getInstance().getFriendManager().isFriend(player.getGameProfile().getName())) continue;
            if (EntityUtil.distanceTo(entity) > range.get()) continue;
            if (living.getHealth() < bestHealth) {
                bestHealth = living.getHealth();
                best = living;
            }
        }
        return best;
    }
}
