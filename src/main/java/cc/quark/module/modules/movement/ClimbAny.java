package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * ClimbAny - climb any solid wall by pressing the jump key while pushing into it.
 *
 * <p>Detects if a solid block is in the direction the player is moving and if so
 * applies an upward velocity to simulate climbing.  This works on any solid
 * surface, not just ladders and vines.
 */
public class ClimbAny extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Upward climb speed (blocks/tick)", 0.25, 0.05, 1.0));
    private final BoolSetting requireJump = register(new BoolSetting(
            "Require Jump", "Only climb when the jump key is held", false));
    private final BoolSetting requireForward = register(new BoolSetting(
            "Require Forward", "Only climb when walking into the wall", true));
    private final BoolSetting noFallDamage = register(new BoolSetting(
            "No Fall Damage", "Reset fall distance while climbing", true));

    public ClimbAny() {
        super("ClimbAny", "Climb any solid wall by holding jump or walking into it", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isOnGround()) return;

        // Key conditions
        if (requireJump.isEnabled() && !mc.player.input.jumping) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (requireForward.isEnabled() && fwd == 0 && side == 0) return;

        // Check if any of the four horizontal neighbours is a solid block
        boolean wallAdjacent = false;
        BlockPos playerPos = mc.player.getBlockPos();
        for (Direction dir : Direction.Type.HORIZONTAL) {
            BlockPos neighbour = playerPos.offset(dir);
            if (!mc.world.getBlockState(neighbour).isAir()) {
                wallAdjacent = true;
                break;
            }
        }

        // Also check using horizontal velocity direction
        if (!wallAdjacent) {
            double yawRad = Math.toRadians(mc.player.getYaw());
            double dxLook = -Math.sin(yawRad) * fwd + Math.cos(yawRad) * side;
            double dzLook =  Math.cos(yawRad) * fwd + Math.sin(yawRad) * side;
            BlockPos frontBlock = playerPos.add(
                    (int) Math.round(dxLook), 0, (int) Math.round(dzLook));
            if (!mc.world.getBlockState(frontBlock).isAir()) {
                wallAdjacent = true;
            }
        }

        if (!wallAdjacent) return;

        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x, speed.get(), v.z);

        if (noFallDamage.isEnabled()) {
            mc.player.fallDistance = 0;
        }
    }
}
