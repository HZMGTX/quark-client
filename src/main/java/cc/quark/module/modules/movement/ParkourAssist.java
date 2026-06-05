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
 * ParkourAssist - detects when a parkour jump is needed and adjusts timing and
 * velocity to improve the chance of making the gap.
 *
 * <p>Behaviour:
 * <ol>
 *   <li>While the player is on the ground and moving, check whether a gap exists
 *       directly ahead (no solid block below the next few blocks).</li>
 *   <li>When a gap is detected within {@code edgeThreshold} blocks of the player's
 *       position, automatically trigger a jump.</li>
 *   <li>Optionally apply a small forward velocity boost at jump time so the player
 *       always reaches the far edge.</li>
 *   <li>Optionally reset fall distance on landing to allow a rapid next jump.</li>
 * </ol>
 */
public class ParkourAssist extends Module {

    private final BoolSetting autoJump = register(new BoolSetting(
            "Auto Jump", "Jump automatically at block edges", true));

    private final DoubleSetting edgeThreshold = register(new DoubleSetting(
            "Edge Threshold", "Distance from edge (0-1) to trigger auto-jump", 0.28, 0.1, 0.5));

    private final BoolSetting forwardBoost = register(new BoolSetting(
            "Forward Boost", "Add a small forward velocity boost at jump", true));

    private final DoubleSetting boostAmount = register(new DoubleSetting(
            "Boost Amount", "Extra forward speed added on jump (blocks/tick)", 0.06, 0.01, 0.3));

    private final BoolSetting sprintAssist = register(new BoolSetting(
            "Sprint Assist", "Force sprint while running toward a gap", true));

    private final BoolSetting resetFall = register(new BoolSetting(
            "Reset Fall", "Zero fallDistance on landing so you can jump again faster", true));

    /** Track previous on-ground state for landing detection. */
    private boolean wasOnGround = false;

    public ParkourAssist() {
        super("ParkourAssist", "Detects parkour gaps and adjusts jump timing and velocity", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isSneaking()) return;

        boolean onGround = mc.player.isOnGround();

        // Reset fall distance on landing
        if (resetFall.isEnabled() && !wasOnGround && onGround) {
            mc.player.fallDistance = 0;
        }
        wasOnGround = onGround;

        if (!onGround) return;

        // Only act when moving
        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        // Edge detection: fractional position within current block
        double fracX = px - Math.floor(px);
        double fracZ = pz - Math.floor(pz);
        double thresh = edgeThreshold.get();

        boolean nearEdgeX = fracX < thresh || fracX > 1.0 - thresh;
        boolean nearEdgeZ = fracZ < thresh || fracZ > 1.0 - thresh;
        if (!nearEdgeX && !nearEdgeZ) return;

        // Look one block ahead in the facing direction and check for a gap below
        BlockPos ahead = BlockPos.ofFloored(px, py - 0.1, pz)
                .offset(mc.player.getHorizontalFacing());
        BlockPos aheadBelow = ahead.down();

        boolean gapAhead = mc.world.getBlockState(ahead).isAir()
                && mc.world.getBlockState(aheadBelow).isAir();

        if (!gapAhead) return;

        // Sprint assist
        if (sprintAssist.isEnabled()) {
            mc.player.setSprinting(true);
        }

        // Auto jump
        if (autoJump.isEnabled()) {
            mc.player.jump();

            // Forward boost in the current look direction
            if (forwardBoost.isEnabled()) {
                double yawRad = Math.toRadians(mc.player.getYaw());
                double boost  = boostAmount.get();
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(
                        vel.x - Math.sin(yawRad) * boost,
                        vel.y,
                        vel.z + Math.cos(yawRad) * boost);
            }
        }
    }
}
