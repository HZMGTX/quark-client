package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Follow extends Module {

    private final ModeSetting target = register(new ModeSetting(
            "Target", "What entity type to follow", "Nearest Player",
            "Nearest Player", "Nearest Mob", "Nearest Entity"));

    private final DoubleSetting stopDist = register(new DoubleSetting(
            "Distance", "Stop following when this close", 3.0, 1.0, 10.0));

    private final BoolSetting sprint = register(new BoolSetting(
            "Sprint", "Sprint while following", true));

    private final BoolSetting jump = register(new BoolSetting(
            "Jump", "Auto-jump over obstacles", true));

    public Follow() {
        super("Follow", "Automatically follows the nearest target entity", Category.WORLD);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.input.movementForward = 0.0f;
            mc.player.setSprinting(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        Entity tgt = findTarget();
        if (tgt == null) {
            mc.player.input.movementForward = 0.0f;
            mc.player.setSprinting(false);
            return;
        }

        double dist = mc.player.distanceTo(tgt);
        if (dist <= stopDist.get()) {
            mc.player.input.movementForward = 0.0f;
            mc.player.setSprinting(false);
            return;
        }

        Vec3d dir = tgt.getPos().subtract(mc.player.getPos()).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        mc.player.setYaw(yaw);
        mc.player.input.movementForward = 1.0f;

        if (sprint.isEnabled() && dist > stopDist.get() + 1.0) {
            mc.player.setSprinting(true);
        }

        if (jump.isEnabled() && mc.player.isOnGround()) {
            BlockPos front = mc.player.getBlockPos().offset(mc.player.getHorizontalFacing());
            BlockState frontState = mc.world.getBlockState(front);
            BlockState frontUp = mc.world.getBlockState(front.up());
            if (!frontState.isAir() && frontUp.isAir()) {
                mc.player.jump();
            } else {
                BlockPos frontDown = mc.player.getBlockPos().offset(mc.player.getHorizontalFacing()).down();
                if (mc.world.getBlockState(frontDown).isAir()
                        && mc.world.getBlockState(frontDown.down()).isAir()) {
                } else if (!frontState.isAir()) {
                    mc.player.jump();
                }
            }
        }
    }

    private Entity findTarget() {
        if (mc.world == null) return null;
        Entity closest = null;
        double best = Double.MAX_VALUE;

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            boolean matches = switch (target.get()) {
                case "Nearest Player" -> e instanceof PlayerEntity;
                case "Nearest Mob"   -> e instanceof MobEntity;
                default              -> e instanceof LivingEntity;
            };
            if (!matches) continue;
            double d = mc.player.distanceTo(e);
            if (d < best) {
                best = d;
                closest = e;
            }
        }
        return closest;
    }
}
