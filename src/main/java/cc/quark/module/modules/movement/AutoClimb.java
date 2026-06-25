package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoClimb extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Climbing speed in blocks per tick", 0.15, 0.01, 1.0));

    public AutoClimb() {
        super("AutoClimb", "Automatically climbs ladders, vines, ropes", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos pos = mc.player.getBlockPos();

        // Check if player is on/adjacent to a climbable block
        boolean onLadder = mc.world.getBlockState(pos).getBlock() == Blocks.LADDER ||
                           mc.world.getBlockState(pos).getBlock() == Blocks.VINE ||
                           mc.world.getBlockState(pos).getBlock() == Blocks.TWISTING_VINES ||
                           mc.world.getBlockState(pos).getBlock() == Blocks.WEEPING_VINES ||
                           mc.world.getBlockState(pos).isOf(Blocks.BAMBOO);

        if (!onLadder) return;

        // Apply forward key press to simulate climbing
        Vec3d vel = mc.player.getVelocity();
        double climbSpeed = speed.get();

        if (mc.options.sneakKey.isPressed()) {
            // Descend
            mc.player.setVelocity(vel.x, -climbSpeed, vel.z);
        } else {
            // Ascend when forward is held or by default
            mc.player.setVelocity(vel.x, climbSpeed, vel.z);
        }

        // Also set forward key to simulate movement
        if (mc.player.input.movementForward > 0) {
            mc.player.setSprinting(false);
        }
    }
}
