package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * AntiWall - detects when the player is about to walk into a solid block in
 * their movement direction and smoothly redirects or stops horizontal motion
 * to prevent getting stuck on walls.
 */
public class AntiWall extends Module {

    private final DoubleSetting lookAhead = register(new DoubleSetting(
            "Look Ahead", "Distance ahead to check for walls (blocks)", 0.4, 0.1, 1.0));

    private final BoolSetting redirect = register(new BoolSetting(
            "Redirect", "Redirect around walls instead of stopping", true));

    private final BoolSetting onlyMoving = register(new BoolSetting(
            "Only Moving", "Only activate while movement keys are held", true));

    public AntiWall() {
        super("AntiWall", "Prevents walking into walls by redirecting movement", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isSneaking()) return;

        if (onlyMoving.isEnabled()) {
            boolean moving = mc.player.input.movementForward != 0
                    || mc.player.input.movementSideways != 0;
            if (!moving) return;
        }

        Vec3d vel = mc.player.getVelocity();
        double hx = vel.x;
        double hz = vel.z;
        if (Math.abs(hx) < 0.001 && Math.abs(hz) < 0.001) return;

        double look = lookAhead.get();
        Vec3d pos = mc.player.getPos();

        // Check the block at the predicted next position
        double nextX = pos.x + hx * look * 5;
        double nextZ = pos.z + hz * look * 5;

        BlockPos nextFeet = new BlockPos((int) Math.floor(nextX), (int) Math.floor(pos.y), (int) Math.floor(nextZ));
        boolean wallFeet = !mc.world.getBlockState(nextFeet).isAir() && mc.world.getBlockState(nextFeet).isSolid();
        boolean wallHead = !mc.world.getBlockState(nextFeet.up()).isAir() && mc.world.getBlockState(nextFeet.up()).isSolid();

        if (!wallFeet && !wallHead) return;

        if (redirect.isEnabled()) {
            // Try sliding along one axis only
            BlockPos xBlock = new BlockPos((int) Math.floor(nextX), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
            BlockPos zBlock = new BlockPos((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(nextZ));

            boolean xBlocked = (!mc.world.getBlockState(xBlock).isAir() && mc.world.getBlockState(xBlock).isSolid())
                    || (!mc.world.getBlockState(xBlock.up()).isAir() && mc.world.getBlockState(xBlock.up()).isSolid());
            boolean zBlocked = (!mc.world.getBlockState(zBlock).isAir() && mc.world.getBlockState(zBlock).isSolid())
                    || (!mc.world.getBlockState(zBlock.up()).isAir() && mc.world.getBlockState(zBlock.up()).isSolid());

            if (xBlocked && !zBlocked) {
                mc.player.setVelocity(0, vel.y, hz);
            } else if (!xBlocked && zBlocked) {
                mc.player.setVelocity(hx, vel.y, 0);
            } else {
                // Both axes blocked — stop horizontal motion
                mc.player.setVelocity(0, vel.y, 0);
            }
        } else {
            mc.player.setVelocity(0, vel.y, 0);
        }
    }
}
