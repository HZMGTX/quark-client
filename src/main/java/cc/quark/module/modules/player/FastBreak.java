package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * FastBreak - speeds up block breaking.
 *
 * Primary method: sends repeated dig packets each tick to accelerate break
 * progress on the server side.
 *
 * Fallback/supplement: applies a client-side Haste status effect at the
 * configured amplifier level so the client also breaks faster.
 *
 * InstaBreak: sends STOP_DESTROY_BLOCK immediately after START_DESTROY_BLOCK
 * in the same tick, which can cause instant breaks on servers with lenient
 * anti-cheat or in singleplayer.
 */
public class FastBreak extends Module {

    public static FastBreak INSTANCE;

    private final IntSetting speedSetting = register(new IntSetting(
            "Speed", "Break speed multiplier (1=normal, 20=instant)", 10, 1, 20));

    private final IntSetting hasteLevel = register(new IntSetting(
            "Haste Level", "Client Haste effect amplifier (1-10)", 2, 1, 10));

    private final BoolSetting onlyTools = register(new BoolSetting(
            "Only Tools", "Only speed up breaking when holding the correct tool", true));

    private final BoolSetting instaBreak = register(new BoolSetting(
            "Insta Break", "Send break+done in same tick for instant breaks", false));

    public FastBreak() {
        super("FastBreak", "Speeds up block breaking", Category.PLAYER);
        INSTANCE = this;
    }

    /** Returns the current speed multiplier. Used by MixinPlayerInteractHandler. */
    public float getSpeedMultiplier() {
        return speedSetting.get();
    }

    /** Returns whether to restrict speedup to correct tools only. */
    public boolean isOnlyTools() {
        return onlyTools.isEnabled();
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

        // Apply Haste effect each tick as a client-side speedup
        mc.player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.HASTE,
                10,                    // duration: 10 ticks (refreshed every tick)
                hasteLevel.get() - 1,  // amplifier is 0-based
                false,
                false                  // no particles
        ));

        // Only act when actively mining (left mouse held)
        if (!mc.options.attackKey.isPressed()) return;

        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos  pos  = blockHit.getBlockPos();
        Direction face = blockHit.getSide();
        BlockState state = mc.world.getBlockState(pos);

        if (state.isAir()) return;

        // Check tool requirement
        if (onlyTools.isEnabled()) {
            float baseSpeed = state.calcBlockBreakingDelta(mc.player, mc.world, pos);
            if (baseSpeed < 0.0001f) return;
        }

        // InstaBreak: immediately send STOP_DESTROY after START_DESTROY
        if (instaBreak.isEnabled()) {
            mc.player.networkHandler.sendPacket(
                    new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, face));
            mc.player.networkHandler.sendPacket(
                    new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, face));
            return;
        }

        // Send repeated block break packets to accelerate server-side progress
        int extra = speedSetting.get() - 1;
        for (int i = 0; i < extra; i++) {
            mc.player.networkHandler.sendPacket(
                    new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, face));
        }
    }
}
