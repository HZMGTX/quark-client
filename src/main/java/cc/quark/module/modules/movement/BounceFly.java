package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * BounceFly - fly by rapidly bouncing off slime blocks.
 * When standing on a slime block the module jumps and adjusts Y velocity
 * to achieve controlled altitude gain.
 */
public class BounceFly extends Module {

    private final DoubleSetting climbSpeed = register(new DoubleSetting(
            "Climb Speed", "Upward velocity burst per bounce (blocks/tick)", 0.6, 0.1, 3.0));
    private final DoubleSetting horizontalSpeed = register(new DoubleSetting(
            "Horizontal Speed", "Horizontal speed multiplier while bouncing", 1.2, 0.5, 4.0));

    private boolean wasOnSlime = false;

    public BounceFly() {
        super("BounceFly", "Fly by bouncing rapidly off slime blocks", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasOnSlime = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos below = mc.player.getBlockPos().down();
        boolean onSlime = mc.world.getBlockState(below).isOf(Blocks.SLIME_BLOCK);

        if (onSlime && mc.player.isOnGround()) {
            // Jump off the slime and apply climb velocity
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(
                    vel.x * horizontalSpeed.get(),
                    climbSpeed.get(),
                    vel.z * horizontalSpeed.get());
            mc.player.fallDistance = 0;
        }

        wasOnSlime = onSlime;
    }
}
