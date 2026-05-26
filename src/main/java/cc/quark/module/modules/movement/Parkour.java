package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;

public class Parkour extends Module {

    private final BoolSetting sprint = register(new BoolSetting(
            "Sprint", "Keep sprinting while parkour is active", true));

    private final DoubleSetting edgeThreshold = register(new DoubleSetting(
            "Edge Threshold", "How far from the block edge to trigger jump (blocks)", 0.25, 0.05, 0.5));

    public Parkour() {
        super("Parkour", "Automatically jumps at the edge of blocks", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.isSneaking()) return;

        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;
        if (!moving) return;

        if (sprint.isEnabled()) mc.player.setSprinting(true);

        double vx = mc.player.getVelocity().x;
        double vz = mc.player.getVelocity().z;

        if (Math.abs(vx) < 0.01 && Math.abs(vz) < 0.01) {
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            vx = -Math.sin(yaw) * 0.1;
            vz =  Math.cos(yaw) * 0.1;
        }

        double len  = Math.sqrt(vx * vx + vz * vz);
        double normX = vx / len;
        double normZ = vz / len;

        double thresh = edgeThreshold.get();
        double checkX = mc.player.getX() + normX * (0.3 + thresh);
        double checkZ = mc.player.getZ() + normZ * (0.3 + thresh);
        double checkY = mc.player.getY();

        BlockPos aheadBelow = new BlockPos(
                (int) Math.floor(checkX),
                (int) Math.floor(checkY) - 1,
                (int) Math.floor(checkZ));

        BlockPos standingOn = new BlockPos(
                (int) Math.floor(mc.player.getX()),
                (int) Math.floor(checkY) - 1,
                (int) Math.floor(mc.player.getZ()));

        boolean currentHasFloor = !mc.world.getBlockState(standingOn).isAir();
        boolean edgeAhead       = mc.world.getBlockState(aheadBelow).isAir();

        if (currentHasFloor && edgeAhead) {
            mc.player.jump();
        }
    }
}
