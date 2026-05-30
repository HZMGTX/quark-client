package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * BouncePad2 - Amplifies bounce velocity when player is on or near honey/slime blocks.
 * Distinct from BouncePad which bounces on any landing.
 */
public class BouncePad2 extends Module {

    private final DoubleSetting bounceMultiplier = register(new DoubleSetting("BounceMultiplier", "Velocity multiplier when bouncing on special blocks", 2.0, 1.0, 5.0));
    private final BoolSetting autoJump = register(new BoolSetting("AutoJump", "Auto-jump when landing on bounce blocks", false));

    private boolean wasInAir = false;

    public BouncePad2() {
        super("BouncePad2", "Amplifies bounces on honey and slime blocks", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasInAir = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean onGround = mc.player.isOnGround();
        boolean onBounceBlock = isOnBounceBlock();

        // Auto jump on bounce blocks
        if (autoJump.isEnabled() && onGround && onBounceBlock) {
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, 0.42, vel.z);
        }

        // Amplify bounce when landing on slime/honey
        if (onGround && wasInAir && onBounceBlock) {
            Vec3d vel = mc.player.getVelocity();
            double mult = bounceMultiplier.get();
            // Amplify the upward velocity on bounce
            mc.player.setVelocity(vel.x * mult, Math.abs(vel.y) * mult * 0.5, vel.z * mult);
            mc.player.fallDistance = 0;
        }

        wasInAir = !onGround;
    }

    private boolean isOnBounceBlock() {
        if (mc.player == null || mc.world == null) return false;
        BlockPos below = mc.player.getBlockPos().down();
        var block = mc.world.getBlockState(below).getBlock();
        return block == Blocks.SLIME_BLOCK || block == Blocks.HONEY_BLOCK;
    }
}
