package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.BlockPos;

/**
 * ParkourHelper - auto-jumps at the last safe moment before the player falls
 * off an edge, ensuring parkour gaps are cleared automatically.
 */
public class ParkourHelper extends Module {

    private final BoolSetting sprint = register(new BoolSetting(
            "Sprint", "Keep sprinting while active", true));
    private final BoolSetting requireForward = register(new BoolSetting(
            "Require Forward", "Only jump when moving forward", true));

    public ParkourHelper() {
        super("ParkourHelper", "Auto-jump at the last safe moment before an edge", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.isSneaking()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        boolean moving = requireForward.isEnabled() ? fwd > 0 : (fwd != 0 || side != 0);
        if (!moving) return;

        if (sprint.isEnabled()) mc.player.setSprinting(true);

        // Look 1.5 blocks ahead in the movement direction
        double vx = mc.player.getVelocity().x;
        double vz = mc.player.getVelocity().z;
        double len = Math.sqrt(vx * vx + vz * vz);

        if (len < 0.01) {
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            vx = -Math.sin(yaw);
            vz =  Math.cos(yaw);
            len = 1.0;
        }

        double nx = vx / len;
        double nz = vz / len;

        double checkX = mc.player.getX() + nx * 1.5;
        double checkZ = mc.player.getZ() + nz * 1.5;
        double checkY = mc.player.getY();

        // Block directly under the look-ahead point
        BlockPos aheadBelow = new BlockPos(
                (int) Math.floor(checkX),
                (int) Math.floor(checkY) - 1,
                (int) Math.floor(checkZ));

        // Also check 2 blocks below (deeper gap)
        BlockPos aheadBelow2 = aheadBelow.down();

        boolean isEdge = mc.world.getBlockState(aheadBelow).isAir()
                      && mc.world.getBlockState(aheadBelow2).isAir();

        if (isEdge) {
            mc.player.jump();
        }
    }
}
