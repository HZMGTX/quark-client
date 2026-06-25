package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoVault extends Module {

    private final DoubleSetting height = register(new DoubleSetting(
            "Height", "Max obstacle height to vault over", 1.5, 0.5, 3.0));

    public AutoVault() {
        super("AutoVault", "Auto-parkours over obstacles", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.forwardSpeed <= 0) return;

        float yaw = (float) Math.toRadians(mc.player.getYaw());
        double nx = -Math.sin(yaw);
        double nz = Math.cos(yaw);

        BlockPos inFront = mc.player.getBlockPos().add((int) Math.round(nx), 0, (int) Math.round(nz));

        double h = height.get();
        for (int dy = 1; dy <= (int) h; dy++) {
            BlockPos check = inFront.up(dy);
            if (!mc.world.getBlockState(check).isAir()) {
                // Obstacle found — jump over it
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(vel.x, 0.45 + (dy - 1) * 0.15, vel.z);
                break;
            }
        }
    }
}
