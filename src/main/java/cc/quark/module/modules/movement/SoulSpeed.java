package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;

/**
 * SoulSpeed - applies a speed boost while walking on soul sand or soul soil.
 */
public class SoulSpeed extends Module {

    public SoulSpeed() {
        super("SoulSpeed", "Speed on soul blocks", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        BlockPos below = mc.player.getBlockPos().down();
        boolean soul = mc.world.getBlockState(below).isOf(Blocks.SOUL_SAND)
                || mc.world.getBlockState(below).isOf(Blocks.SOUL_SOIL);
        if (!soul) return;
        mc.player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SPEED, 20, 2, false, false));
    }
}
