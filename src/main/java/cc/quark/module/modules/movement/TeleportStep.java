package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TeleportStep extends Module {

    private final IntSetting maxHeight = register(new IntSetting(
            "Max Height", "Maximum block height to teleport step over", 3, 1, 5));

    public TeleportStep() {
        super("TeleportStep", "Teleports up to a block above obstacle instead of stepping",
                Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;

        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;
        if (!moving) return;

        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        Vec3d pos = mc.player.getPos();
        BlockPos frontBlock = new BlockPos(
                (int) Math.floor(pos.x + dirX * 0.6),
                (int) Math.floor(pos.y),
                (int) Math.floor(pos.z + dirZ * 0.6)
        );

        // Find the top of the obstacle in front
        int maxH = maxHeight.get();
        for (int h = 1; h <= maxH; h++) {
            BlockPos checkPos = frontBlock.up(h);
            if (mc.world.getBlockState(checkPos).isAir()
                    && mc.world.getBlockState(checkPos.up()).isAir()) {
                // Obstacle top found — teleport player here
                mc.player.setPos(pos.x, pos.y + h, pos.z);
                mc.player.setVelocity(mc.player.getVelocity().x, 0, mc.player.getVelocity().z);
                mc.player.fallDistance = 0;
                return;
            }
        }
    }
}
