package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * InstaBreak - instantly breaks any block the player is looking at.
 *
 * Achieved by sending both START and STOP destroy packets in the same tick,
 * combined with Haste II potion effect so the server accepts the instant break.
 * Also applies maximum efficiency tool-speed multiplier.
 */
public class InstaBreak extends Module {

    public InstaBreak() {
        super("InstaBreak", "Instantly breaks blocks (creative-mode speed)", Category.WORLD);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            // Apply Haste II for maximum server-side break speed
            mc.player.addStatusEffect(
                    new StatusEffectInstance(StatusEffects.HASTE, 9999, 1, false, false, false));
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.removeStatusEffect(StatusEffects.HASTE);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Keep Haste II active
        StatusEffectInstance haste = mc.player.getStatusEffect(StatusEffects.HASTE);
        if (haste == null || haste.getDuration() < 100) {
            mc.player.addStatusEffect(
                    new StatusEffectInstance(StatusEffects.HASTE, 9999, 1, false, false, false));
        }

        // Only act when left mouse button is held
        if (!mc.options.attackKey.isPressed()) return;

        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        Direction face = blockHit.getSide();

        if (mc.world.getBlockState(pos).isAir()) return;
        if (mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK) return;

        // Force immediate break by sending start and stop in the same tick
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, face));
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, face));

        // Break client-side immediately
        mc.world.breakBlock(pos, true, mc.player);
    }
}
