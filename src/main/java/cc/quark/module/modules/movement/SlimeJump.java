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

public class SlimeJump extends Module {

    private final BoolSetting autoJump = register(new BoolSetting(
            "AutoJump", "Automatically jump when standing on slime blocks", true));

    private final DoubleSetting heightBoost = register(new DoubleSetting(
            "Height Boost", "Extra upward velocity added on slime bounce", 0.6, 0.0, 3.0));

    public SlimeJump() {
        super("SlimeJump", "Jumps on slime blocks for extra height", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;

        BlockPos below = mc.player.getBlockPos().down();
        if (mc.world.getBlockState(below).getBlock() != Blocks.SLIME_BLOCK) return;

        if (autoJump.isEnabled()) {
            mc.player.jump();
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, vel.y + heightBoost.get(), vel.z);
        } else {
            // Only boost when jump key is held
            if (mc.options.jumpKey.isPressed()) {
                mc.player.jump();
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(vel.x, vel.y + heightBoost.get(), vel.z);
            }
        }
    }
}
