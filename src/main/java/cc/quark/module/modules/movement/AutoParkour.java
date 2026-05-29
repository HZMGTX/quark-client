package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * AutoParkour - scans for parkour gaps ahead and automatically triggers a jump
 * at the optimal moment to clear the gap.
 *
 * <p>Extends the lookahead beyond the simpler {@link ParkourHelper} by scanning
 * multiple blocks ahead based on the {@code lookAhead} setting.
 */
public class AutoParkour extends Module {

    private final IntSetting lookAhead = register(new IntSetting(
            "Look Ahead", "Number of blocks to scan ahead for gaps", 3, 1, 8));
    private final BoolSetting autoSprint = register(new BoolSetting(
            "Auto Sprint", "Keep sprinting while active", true));
    private final BoolSetting sneakCancel = register(new BoolSetting(
            "Sneak Cancel", "Disable auto-jump while sneaking", true));

    public AutoParkour() {
        super("AutoParkour", "Detect parkour gaps and auto-jump at the right moment", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;
        if (sneakCancel.isEnabled() && mc.player.isSneaking()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        if (autoSprint.isEnabled()) mc.player.setSprinting(true);

        // Determine movement direction
        Vec3d vel = mc.player.getVelocity();
        double vx = vel.x, vz = vel.z;
        double hLen = Math.sqrt(vx * vx + vz * vz);

        if (hLen < 0.01) {
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            vx = -Math.sin(yaw);
            vz =  Math.cos(yaw);
            hLen = 1.0;
        }

        double nx = vx / hLen;
        double nz = vz / hLen;

        // Scan ahead for the first gap
        int look = lookAhead.get();
        for (int i = 1; i <= look; i++) {
            double checkX = mc.player.getX() + nx * i;
            double checkZ = mc.player.getZ() + nz * i;
            double checkY = mc.player.getY();

            BlockPos aheadBelow = new BlockPos(
                    (int) Math.floor(checkX),
                    (int) Math.floor(checkY) - 1,
                    (int) Math.floor(checkZ));

            if (mc.world.getBlockState(aheadBelow).isAir()) {
                // Gap found — jump now if we're at the last solid step
                BlockPos currentBelow = new BlockPos(
                        (int) Math.floor(mc.player.getX()),
                        (int) Math.floor(checkY) - 1,
                        (int) Math.floor(mc.player.getZ()));
                if (!mc.world.getBlockState(currentBelow).isAir()) {
                    mc.player.jump();
                }
                break;
            }
        }
    }
}
