package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TunnelFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Fly speed through 2-block tunnels", 0.2, 0.05, 1.0));

    public TunnelFly() {
        super("TunnelFly", "Fly through 2-high tunnels automatically", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos pos = mc.player.getBlockPos();
        boolean tunnel = mc.world.getBlockState(pos).isAir()
                && mc.world.getBlockState(pos.up()).isAir()
                && !mc.world.getBlockState(pos.up(2)).isAir();

        if (!tunnel) return;

        float yaw = (float) Math.toRadians(mc.player.getYaw());
        double dx = -Math.sin(yaw) * speed.get();
        double dz = Math.cos(yaw) * speed.get();

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(dx, vel.y, dz);
    }
}
