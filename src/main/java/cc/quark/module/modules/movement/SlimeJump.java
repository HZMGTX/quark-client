package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SlimeJump extends Module {

    private final IntSetting maxBounce = register(new IntSetting(
            "MaxBounce", "Maximum number of consecutive bounces", 5, 2, 10));

    private int bounceCount = 0;
    private boolean wasBouncing = false;

    public SlimeJump() {
        super("SlimeJump", "Bounces on slime blocks infinitely/controllably", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        bounceCount = 0;
        wasBouncing = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc == null || mc.player == null || mc.world == null) return;

        BlockPos below = mc.player.getBlockPos().down();
        boolean onSlime = mc.world.getBlockState(below).getBlock() == Blocks.SLIME_BLOCK;

        if (onSlime && mc.player.isOnGround()) {
            if (!wasBouncing) {
                bounceCount = 0;
                wasBouncing = true;
            }
            if (bounceCount < maxBounce.get() && !mc.player.isSneaking()) {
                Vec3d vel = mc.player.getVelocity();
                double bounceY = Math.max(0.4, Math.abs(vel.y) * 0.85 + 0.3);
                mc.player.setVelocity(vel.x, bounceY, vel.z);
                bounceCount++;
            }
        } else if (!onSlime) {
            if (wasBouncing) {
                bounceCount = 0;
                wasBouncing = false;
            }
        }
    }
}
