package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ParkourBoost extends Module {

    private final DoubleSetting boost = register(new DoubleSetting(
            "Boost", "Extra velocity at parkour edge", 0.1, 0.0, 0.5));

    private final BoolSetting autoDetect = register(new BoolSetting(
            "Auto Detect", "Automatically detect parkour edges", true));

    public ParkourBoost() {
        super("ParkourBoost", "Extra boost on parkour edges", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;

        if (autoDetect.isEnabled()) {
            // Check if player is near edge (block below is missing in forward direction)
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            double nextX = mc.player.getX() - Math.sin(yaw) * 1.5;
            double nextZ = mc.player.getZ() + Math.cos(yaw) * 1.5;
            BlockPos forward = new BlockPos((int) nextX, mc.player.getBlockY() - 1, (int) nextZ);

            if (mc.world.getBlockState(forward).isAir()) {
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(
                        vel.x - Math.sin(yaw) * boost.get(),
                        vel.y,
                        vel.z + Math.cos(yaw) * boost.get()
                );
            }
        }
    }
}
