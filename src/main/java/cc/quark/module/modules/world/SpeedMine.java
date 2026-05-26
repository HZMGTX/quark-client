package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SpeedMine extends Module {

    private final IntSetting hasteLevel = register(new IntSetting(
            "Haste Level", "Haste amplifier (1 = Haste I, 2 = Haste II, etc.)", 2, 1, 10));

    private final BoolSetting removeFatigue = register(new BoolSetting(
            "Remove Fatigue", "Remove mining fatigue effect", true));

    private final BoolSetting packetMode = register(new BoolSetting(
            "Packet Mode", "Use packet trick to instantly break targeted block", false));

    private BlockPos lastPacketPos = null;

    public SpeedMine() {
        super("SpeedMine", "Applies Haste and removes Mining Fatigue for faster mining", Category.WORLD);
    }

    @Override
    public void onDisable() {
        lastPacketPos = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (removeFatigue.isEnabled() && mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            mc.player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        }

        int amp = hasteLevel.get() - 1;
        StatusEffectInstance current = mc.player.getStatusEffect(StatusEffects.HASTE);
        if (current == null || current.getAmplifier() < amp) {
            mc.player.addStatusEffect(
                    new StatusEffectInstance(StatusEffects.HASTE, 100, amp, false, false));
        }

        if (packetMode.isEnabled() && mc.world != null && mc.interactionManager != null) {
            if (!mc.options.attackKey.isPressed()) {
                lastPacketPos = null;
                return;
            }

            HitResult hit = mc.crosshairTarget;
            if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

            BlockHitResult bhr = (BlockHitResult) hit;
            BlockPos pos = bhr.getBlockPos();
            Direction face = bhr.getSide();

            if (mc.world.getBlockState(pos).isAir()) return;
            if (mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK) return;
            if (mc.world.getBlockState(pos).getHardness(mc.world, pos) < 0) return;

            if (!pos.equals(lastPacketPos)) {
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, face));
                lastPacketPos = pos;
            }

            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, face));

            mc.interactionManager.attackBlock(pos, face);
        }
    }
}
