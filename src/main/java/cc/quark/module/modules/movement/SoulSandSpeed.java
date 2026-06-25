package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SoulSandSpeed extends Module {

    private final BoolSetting magmaSpeed = register(new BoolSetting(
            "MagmaSpeed", "Also cancel slow on magma blocks", true));

    public SoulSandSpeed() {
        super("SoulSandSpeed", "Negates soul sand slow while walking on it", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;

        BlockPos below = mc.player.getBlockPos().down();
        Block block = mc.world.getBlockState(below).getBlock();

        boolean onSoulSand = block == Blocks.SOUL_SAND || block == Blocks.SOUL_SOIL;
        boolean onMagma = magmaSpeed.isEnabled() && block == Blocks.MAGMA_BLOCK;

        if (!onSoulSand && !onMagma) return;

        Vec3d vel = mc.player.getVelocity();
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (hSpeed < 0.01) return;

        // Soul sand reduces speed by ~40%, restore it
        double boost = onSoulSand ? 1.65 : 1.3;
        mc.player.setVelocity(vel.x * boost, vel.y, vel.z * boost);
    }
}
