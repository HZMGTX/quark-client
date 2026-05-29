package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

/**
 * NoWeb - when inside a cobweb (or berry bush), set movement velocity to normal
 * walk speed ignoring the slowdown by computing direction from input and yaw.
 */
public class NoWeb extends Module {

    private final BoolSetting berryBush = register(new BoolSetting(
            "Berry Bush", "Also bypass sweet berry bush slowdown", true));
    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Override speed inside web (blocks/tick)", 0.2, 0.05, 0.5));

    public NoWeb() {
        super("NoWeb", "Move at full speed through cobwebs and berry bushes", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos feet = mc.player.getBlockPos();
        boolean inWeb  = mc.world.getBlockState(feet).isOf(Blocks.COBWEB);
        boolean inBush = berryBush.isEnabled()
                      && mc.world.getBlockState(feet).isOf(Blocks.SWEET_BERRY_BUSH);

        if (!inWeb && !inBush) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        double yawRad = Math.toRadians(mc.player.getYaw());
        double s = speed.get();

        double len = Math.sqrt(fwd * fwd + side * side);
        double normFwd  = fwd  / len;
        double normSide = side / len;

        double nx = (-Math.sin(yawRad) * normFwd + Math.cos(yawRad) * normSide) * s;
        double nz = ( Math.cos(yawRad) * normFwd + Math.sin(yawRad) * normSide) * s;

        mc.player.setVelocity(nx, mc.player.getVelocity().y, nz);
    }
}
