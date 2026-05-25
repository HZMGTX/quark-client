package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class NoWeb extends Module {

    private final BoolSetting berryBush = register(new BoolSetting(
            "Berry Bush", "Also bypass sweet berry bush slowdown", true));

    public NoWeb() {
        super("NoWeb", "Move at full speed through cobwebs and berry bushes", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos feet = mc.player.getBlockPos();
        boolean inWeb = mc.world.getBlockState(feet).isOf(Blocks.COBWEB);
        boolean inBush = berryBush.isEnabled()
                      && mc.world.getBlockState(feet).isOf(Blocks.SWEET_BERRY_BUSH);

        if (!inWeb && !inBush) return;

        Vec3d vel = mc.player.getVelocity();
        float yaw  = mc.player.getYaw();
        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        if (fwd == 0 && side == 0) return;

        double rad = Math.toRadians(yaw);
        double spd = 0.2;
        double nx = -Math.sin(rad) * fwd * spd + Math.cos(rad) * side * spd;
        double nz =  Math.cos(rad) * fwd * spd + Math.sin(rad) * side * spd;
        mc.player.setVelocity(nx, vel.y, nz);
    }
}
