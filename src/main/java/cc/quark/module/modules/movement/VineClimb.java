package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Vec3d;

/**
 * VineClimb - automatically climbs vines and increases vine climbing speed.
 * The player moves upward automatically while touching vines without needing
 * to hold the jump key.
 */
public class VineClimb extends Module {

    private final DoubleSetting climbSpeed = register(new DoubleSetting(
            "Climb Speed", "Upward speed while on vines (blocks/tick)", 0.2, 0.05, 2.0));

    public VineClimb() {
        super("VineClimb", "Climb vines automatically and faster", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        Block block = mc.world.getBlockState(mc.player.getBlockPos()).getBlock();
        boolean onVine = (block == Blocks.VINE || block == Blocks.CAVE_VINES
                || block == Blocks.CAVE_VINES_PLANT || block == Blocks.TWISTING_VINES
                || block == Blocks.TWISTING_VINES_PLANT || block == Blocks.WEEPING_VINES
                || block == Blocks.WEEPING_VINES_PLANT);

        if (!onVine) return;

        Vec3d vel = mc.player.getVelocity();
        double vy = mc.options.sneakKey.isPressed() ? -climbSpeed.get() : climbSpeed.get();

        mc.player.setVelocity(vel.x, vy, vel.z);
        mc.player.fallDistance = 0;
    }
}
