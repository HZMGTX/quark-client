package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class CobwebBypass extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Horizontal speed while in cobweb", 0.2, 0.05, 1.0));

    public CobwebBypass() {
        super("CobwebBypass", "Bypasses cobweb slowdown by applying counter-velocity", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos pos = mc.player.getBlockPos();
        boolean inCobweb = mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB;
        if (!inCobweb) return;

        boolean moving = mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
        if (!moving) return;

        float yaw = (float) Math.toRadians(mc.player.getYaw());
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        double len = Math.sqrt(fwd * fwd + side * side);
        double nf = fwd / len;
        double ns = side / len;

        double vx = (-Math.sin(yaw) * nf + Math.cos(yaw) * ns) * speed.get();
        double vz = (Math.cos(yaw) * nf + Math.sin(yaw) * ns) * speed.get();

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vx, vel.y, vz);
    }
}
