package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;

/**
 * SoulSpeed - on soul sand/soil apply a Speed effect at the configured amplifier
 * to simulate the Soul Speed enchantment.
 */
public class SoulSpeed extends Module {

    private final IntSetting level = register(new IntSetting(
            "Level", "Soul Speed enchantment level (1-3)", 2, 1, 3));

    public SoulSpeed() {
        super("SoulSpeed", "Speed boost on soul sand/soil matching Soul Speed enchantment", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.removeStatusEffect(StatusEffects.SPEED);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos below = mc.player.getBlockPos().down();
        boolean onSoul = mc.world.getBlockState(below).isOf(Blocks.SOUL_SAND)
                      || mc.world.getBlockState(below).isOf(Blocks.SOUL_SOIL);

        if (onSoul) {
            // Level 1 = amplifier 0, level 2 = amplifier 1, level 3 = amplifier 2
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED, 40, level.get() - 1, false, false));
        }
    }
}
