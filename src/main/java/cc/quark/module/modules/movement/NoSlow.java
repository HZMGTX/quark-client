package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class NoSlow extends Module {

    private final BoolSetting usingItems = register(new BoolSetting(
            "Using Items", "Remove slowdown while using items (food, bows, shields)", true));

    private final BoolSetting soulsand = register(new BoolSetting(
            "Soulsand", "Remove slowdown on soul sand", true));

    private final BoolSetting cobweb = register(new BoolSetting(
            "Cobweb", "Remove slowdown in cobwebs", true));

    public NoSlow() {
        super("NoSlow", "Removes movement slowdowns from items, soulsand, and cobwebs", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Remove item-use slowdown by forcing sprint and boosting velocity
        if (usingItems.isEnabled() && mc.player.isUsingItem()) {
            if (mc.player.input.movementForward > 0 || mc.player.input.movementSideways != 0) {
                mc.player.setSprinting(true);
                Vec3d vel = mc.player.getVelocity();
                double[] dir = getMovementDirection();
                // Normal walking speed is ~0.215, item use reduces to ~0.13 so boost back
                double targetSpeed = 0.215;
                double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
                if (hSpeed < targetSpeed) {
                    mc.player.setVelocity(dir[0] * targetSpeed, vel.y, dir[1] * targetSpeed);
                }
            }
        }

        // Remove soulsand slowdown
        if (soulsand.isEnabled()) {
            BlockPos belowPos = mc.player.getBlockPos().down();
            if (mc.world != null && mc.world.getBlockState(belowPos).getBlock() == Blocks.SOUL_SAND) {
                Vec3d vel = mc.player.getVelocity();
                if (mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0) {
                    double[] dir = getMovementDirection();
                    double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
                    // Soulsand reduces speed to ~0.4x normal, compensate
                    if (hSpeed < 0.18) {
                        mc.player.setVelocity(dir[0] * 0.215, vel.y, dir[1] * 0.215);
                    }
                }
            }
        }

        // Remove cobweb slowdown
        if (cobweb.isEnabled()) {
            BlockPos inPos = mc.player.getBlockPos();
            if (mc.world != null && mc.world.getBlockState(inPos).getBlock() == Blocks.COBWEB) {
                Vec3d vel = mc.player.getVelocity();
                if (mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0) {
                    double[] dir = getMovementDirection();
                    // Cobweb kills velocity almost entirely each tick, so force it
                    mc.player.setVelocity(dir[0] * 0.15, vel.y, dir[1] * 0.15);
                }
            }
        }
    }

    private double[] getMovementDirection() {
        float yaw = mc.player.getYaw();
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        double yawRad = Math.toRadians(yaw);

        if (fwd == 0 && side == 0) {
            return new double[]{ -Math.sin(yawRad), Math.cos(yawRad) };
        }

        double len = Math.sqrt(fwd * fwd + side * side);
        double nFwd = fwd / len;
        double nSide = side / len;
        double x = (-Math.sin(yawRad) * nFwd + Math.cos(yawRad) * nSide);
        double z = (Math.cos(yawRad) * nFwd + Math.sin(yawRad) * nSide);
        return new double[]{ x, z };
    }
}
