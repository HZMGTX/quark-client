package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SlimeBoost extends Module {

    private final DoubleSetting multiply = register(new DoubleSetting(
            "Multiply", "Velocity multiplier when bouncing off slime", 2.5, 1.0, 5.0));

    public SlimeBoost() {
        super("SlimeBoost", "Boost off slime blocks", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos below = mc.player.getBlockPos().down();
        if (mc.world.getBlockState(below).getBlock() == Blocks.SLIME_BLOCK) {
            Vec3d vel = mc.player.getVelocity();
            if (vel.y < -0.1) {
                mc.player.setVelocity(vel.x * multiply.get(), Math.abs(vel.y) * multiply.get(), vel.z * multiply.get());
            }
        }
    }
}
