package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

/**
 * AutoShoot — automatically aims and fires ranged weapons at nearby targets.
 */
public class AutoShoot extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Maximum range to find targets", 20.0, 5.0, 50.0));

    private final BoolSetting autoCharge = register(new BoolSetting(
            "AutoCharge", "Automatically charge the bow before firing", true));

    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "OnlyPlayers", "Only target other players", true));

    private int chargeTimer = 0;

    public AutoShoot() {
        super("AutoShoot", "Automatically aims and fires ranged weapons", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.options.useKey.setPressed(false);
        }
        chargeTimer = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        ItemStack held = getHeldRangedWeapon();
        if (held == null) {
            mc.options.useKey.setPressed(false);
            chargeTimer = 0;
            return;
        }

        LivingEntity target = findTarget();
        if (target == null) {
            mc.options.useKey.setPressed(false);
            chargeTimer = 0;
            return;
        }

        // Aim at target (account for projectile drop)
        aimAtTarget(target);

        if (autoCharge.isEnabled()) {
            chargeTimer++;
            mc.options.useKey.setPressed(true);

            // For bow: need ~20 ticks for full charge; for crossbow: just load
            boolean isBow = held.getItem() instanceof BowItem;
            boolean isCrossbow = held.getItem() instanceof CrossbowItem;

            if (isBow && chargeTimer >= 20) {
                // Release to fire
                mc.options.useKey.setPressed(false);
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                chargeTimer = 0;
            } else if (isCrossbow) {
                // Crossbow fires when fully loaded
                if (CrossbowItem.isCharged(held)) {
                    mc.options.useKey.setPressed(false);
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    chargeTimer = 0;
                }
            }
        } else {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }

    private ItemStack getHeldRangedWeapon() {
        if (mc.player == null) return null;
        ItemStack main = mc.player.getMainHandStack();
        if (main.getItem() instanceof BowItem || main.getItem() instanceof CrossbowItem) return main;
        ItemStack off = mc.player.getOffHandStack();
        if (off.getItem() instanceof BowItem || off.getItem() instanceof CrossbowItem) return off;
        return null;
    }

    private LivingEntity findTarget() {
        List<LivingEntity> candidates = EntityUtil.getEntitiesOfType(LivingEntity.class, range.get());
        if (onlyPlayers.isEnabled()) {
            candidates.removeIf(e -> !(e instanceof PlayerEntity));
        }
        candidates.removeIf(e -> e == mc.player);
        candidates.removeIf(EntityUtil::isFriend);
        if (candidates.isEmpty()) return null;
        candidates.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
        return candidates.get(0);
    }

    private void aimAtTarget(LivingEntity target) {
        if (mc.player == null) return;
        Vec3d eyes = mc.player.getEyePos();
        Vec3d targetPos = target.getEyePos();

        double dx = targetPos.x - eyes.x;
        double dy = targetPos.y - eyes.y;
        double dz = targetPos.z - eyes.z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        // Basic gravity compensation for bow (approx)
        double gravComp = dist * 0.025;
        dy += gravComp;

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
        mc.player.headYaw = yaw;
    }
}
