package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ReverseStep extends Module {

    private final DoubleSetting height = register(new DoubleSetting(
            "Height", "Max block height to step down smoothly", 1.0, 0.5, 3.0));

    public ReverseStep() {
        super("ReverseStep", "Steps down ledges smoothly without taking fall damage", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.getVelocity().y < -0.1) return;

        Vec3d vel = mc.player.getVelocity();
        if (Math.abs(vel.x) < 0.01 && Math.abs(vel.z) < 0.01) return;

        BlockPos below = mc.player.getBlockPos().down();
        boolean solidBelow = !mc.world.getBlockState(below).isAir();

        if (!solidBelow) {
            double dropY = Math.min(height.get(), 1.0);
            mc.player.setVelocity(vel.x, -dropY, vel.z);
            mc.player.fallDistance = 0;
        }
    }
}
