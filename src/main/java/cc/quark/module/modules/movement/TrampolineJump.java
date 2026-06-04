package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * TrampolineJump - bounces the player high when landing on slime blocks,
 * simulating a trampoline effect by applying a strong upward velocity on landing.
 *
 * <p>A landing is detected when the player transitions from airborne to on-ground
 * while above a slime or honey block.
 */
public class TrampolineJump extends Module {

    private final DoubleSetting bounce = register(new DoubleSetting(
            "Bounce", "Upward velocity applied when bouncing off slime", 2.5, 0.5, 15.0));

    private final BoolSetting honeyBlock = register(new BoolSetting(
            "Honey Block", "Also bounce on honey blocks", false));

    private final BoolSetting noFallDamage = register(new BoolSetting(
            "No Fall Damage", "Reset fall distance to prevent landing damage", true));

    private boolean wasInAir = false;

    public TrampolineJump() {
        super("TrampolineJump", "Bounces you high when landing on slime", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasInAir = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean onGround = mc.player.isOnGround();

        if (onGround && wasInAir) {
            BlockPos below = mc.player.getBlockPos().down();
            Block block = mc.world.getBlockState(below).getBlock();

            boolean onSlime = (block == Blocks.SLIME_BLOCK)
                    || (honeyBlock.isEnabled() && block == Blocks.HONEY_BLOCK);

            if (onSlime) {
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(vel.x, bounce.get(), vel.z);
                if (noFallDamage.isEnabled()) {
                    mc.player.fallDistance = 0;
                }
            }
        }

        wasInAir = !onGround;
    }
}
