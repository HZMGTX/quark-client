package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

/**
 * Webless - when inside a cobweb, apply normal movement speed by overriding
 * velocity from player input and yaw, bypassing the cobweb slowdown.
 * Separate from NoWeb; uses EventTick with direct velocity set.
 */
public class Webless extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Override speed in cobwebs (blocks/tick)", 0.18, 0.05, 0.5));

    public Webless() {
        super("Webless", "Normal movement speed inside cobwebs", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos pos = mc.player.getBlockPos();
        if (!mc.world.getBlockState(pos).isOf(Blocks.COBWEB)) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        double yawRad = Math.toRadians(mc.player.getYaw());
        double len = Math.sqrt(fwd * fwd + side * side);
        double normFwd  = fwd  / len;
        double normSide = side / len;

        double s  = speed.get();
        double nx = (-Math.sin(yawRad) * normFwd + Math.cos(yawRad) * normSide) * s;
        double nz = ( Math.cos(yawRad) * normFwd + Math.sin(yawRad) * normSide) * s;

        mc.player.setVelocity(nx, mc.player.getVelocity().y, nz);
    }
}
