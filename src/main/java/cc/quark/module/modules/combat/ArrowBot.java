package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.EntityUtil;
import cc.quark.util.RotationUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

public class ArrowBot extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 20.0, 5.0, 50.0));
    private final IntSetting holdTime = register(new IntSetting("Hold Time", "Milliseconds to hold bow before releasing", 800, 100, 2000));
    private final BoolSetting predictMovement = register(new BoolSetting("Predict Movement", "Lead target position for moving entities", true));

    private long bowHeldSince = 0L;
    private boolean holding = false;

    public ArrowBot() {
        super("ArrowBot", "Auto-aims and shoots arrows/bows at targets", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        holding = false;
        bowHeldSince = 0L;
    }

    @Override
    public void onDisable() {
        if (mc.player != null && holding) {
            mc.interactionManager.stopUsingItem(mc.player);
        }
        holding = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Check if holding bow or crossbow
        var mainHand = mc.player.getMainHandStack();
        var offHand = mc.player.getOffHandStack();
        boolean hasBow = mainHand.getItem() instanceof BowItem
                || offHand.getItem() instanceof BowItem
                || mainHand.getItem() instanceof CrossbowItem;

        if (!hasBow) {
            holding = false;
            return;
        }

        // Find nearest player target
        List<LivingEntity> targets = EntityUtil.getEntitiesOfType(LivingEntity.class, range.get());
        targets.removeIf(e -> e == mc.player || e.isRemoved() || e.getHealth() <= 0f || !(e instanceof PlayerEntity));
        targets.removeIf(EntityUtil::isFriend);

        if (targets.isEmpty()) {
            if (holding) {
                mc.interactionManager.stopUsingItem(mc.player);
                holding = false;
            }
            return;
        }

        targets.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
        LivingEntity target = targets.get(0);

        Vec3d aimPos = target.getEyePos();
        if (predictMovement.isEnabled()) {
            double dist = EntityUtil.distanceTo(target);
            double travelTicks = dist / 3.0;
            aimPos = aimPos.add(target.getVelocity().multiply(travelTicks));
        }

        float yaw = RotationUtil.getYaw(aimPos);
        float pitch = RotationUtil.getPitch(aimPos);
        mc.player.setYaw(yaw);
        mc.player.setPitch(MathHelper.clamp(pitch, -90f, 90f));

        if (!holding) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            bowHeldSince = System.currentTimeMillis();
            holding = true;
        } else if (System.currentTimeMillis() - bowHeldSince >= holdTime.get()) {
            mc.interactionManager.stopUsingItem(mc.player);
            holding = false;
        }
    }
}
