package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class NoCobweb extends Module {

    public NoCobweb() {
        super("NoCobweb", "Prevents cobwebs from slowing movement", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos pos = mc.player.getBlockPos();
        boolean inCobweb = mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB;
        if (!inCobweb) return;

        Vec3d vel = mc.player.getVelocity();
        // Cobweb clamps velocity to ~0.05 — restore to normal walk speed
        if (Math.abs(vel.x) < 0.1 || Math.abs(vel.z) < 0.1) {
            boolean moving = mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
            if (moving) {
                float yaw = (float) Math.toRadians(mc.player.getYaw());
                float fwd = mc.player.input.movementForward;
                float side = mc.player.input.movementSideways;
                double len = Math.max(1.0, Math.sqrt(fwd * fwd + side * side));
                double vx = (-Math.sin(yaw) * (fwd / len) + Math.cos(yaw) * (side / len)) * 0.215;
                double vz = (Math.cos(yaw) * (fwd / len) + Math.sin(yaw) * (side / len)) * 0.215;
                mc.player.setVelocity(vx, vel.y, vz);
            }
        }
    }
}
