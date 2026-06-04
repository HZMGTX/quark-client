package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * CarpetFly - allows the player to fly when standing on any carpet block by
 * applying upward velocity while jump is held.
 */
public class CarpetFly extends Module {

    private final DoubleSetting flySpeed = register(new DoubleSetting(
            "Fly Speed", "Vertical and horizontal fly speed (blocks/tick)", 0.5, 0.05, 3.0));

    public CarpetFly() {
        super("CarpetFly", "Allows flying when standing on carpet", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos below = mc.player.getBlockPos().down();
        Block block = mc.world.getBlockState(below).getBlock();

        boolean onCarpet = (block == Blocks.WHITE_CARPET || block == Blocks.ORANGE_CARPET
                || block == Blocks.MAGENTA_CARPET || block == Blocks.LIGHT_BLUE_CARPET
                || block == Blocks.YELLOW_CARPET || block == Blocks.LIME_CARPET
                || block == Blocks.PINK_CARPET || block == Blocks.GRAY_CARPET
                || block == Blocks.LIGHT_GRAY_CARPET || block == Blocks.CYAN_CARPET
                || block == Blocks.PURPLE_CARPET || block == Blocks.BLUE_CARPET
                || block == Blocks.BROWN_CARPET || block == Blocks.GREEN_CARPET
                || block == Blocks.RED_CARPET || block == Blocks.BLACK_CARPET);

        if (!onCarpet) return;

        double spd = flySpeed.get();
        Vec3d vel = mc.player.getVelocity();

        double dy = 0;
        if (mc.options.jumpKey.isPressed()) dy = spd;
        else if (mc.options.sneakKey.isPressed()) dy = -spd;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        double yawRad = Math.toRadians(mc.player.getYaw());
        double dx = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * spd;
        double dz = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * spd;

        mc.player.setVelocity(dx, dy, dz);
        mc.player.fallDistance = 0;
    }
}
