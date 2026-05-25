package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AntiCactus extends Module {

    private final BoolSetting berryBush = register(new BoolSetting("Berry Bush", "Also dodge berry bushes", true));
    private final BoolSetting autoMove  = register(new BoolSetting("Auto Escape","Move away from hazard automatically", true));

    public AntiCactus() {
        super("AntiCactus", "Prevents damage from cacti and sweet berry bushes by auto-escaping", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos feet = mc.player.getBlockPos();
        boolean inCactus = mc.world.getBlockState(feet).isOf(Blocks.CACTUS)
                        || mc.world.getBlockState(feet.up()).isOf(Blocks.CACTUS);
        boolean inBerry  = berryBush.isEnabled()
                        && mc.world.getBlockState(feet).isOf(Blocks.SWEET_BERRY_BUSH);

        if (!inCactus && !inBerry) return;

        // Apply regeneration to counteract damage
        mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 1, false, false));

        if (autoMove.isEnabled()) {
            // Push player backwards out of the block
            Vec3d vel = mc.player.getVelocity();
            float yaw = (float) Math.toRadians(mc.player.getYaw() + 180);
            mc.player.setVelocity(Math.sin(-yaw) * 0.3, vel.y, Math.cos(yaw) * 0.3);
        }
    }
}
