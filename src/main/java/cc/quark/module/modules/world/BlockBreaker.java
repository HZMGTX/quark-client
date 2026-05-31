package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BlockBreaker extends Module {

    private final DoubleSetting speedMultiplier = register(new DoubleSetting(
            "Speed", "Mining speed multiplier (higher = faster)", 2.0, 1.0, 10.0));

    private final BoolSetting autoTool = register(new BoolSetting(
            "AutoTool", "Switch to best tool automatically", true));

    private final BoolSetting noSway = register(new BoolSetting(
            "NoSway", "Suppress arm sway animation", false));

    private final IntSetting packetCount = register(new IntSetting(
            "Packets", "Stop-destroy packets per tick", 1, 1, 5));

    private final TimerUtil timer = new TimerUtil();
    private BlockPos lastPos = null;
    private int savedSlot = -1;

    public BlockBreaker() {
        super("BlockBreaker", "Breaks the targeted block faster using packet manipulation", Category.WORLD);
    }

    @Override
    public void onDisable() {
        // Restore hotbar slot if we auto-switched
        if (savedSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = savedSlot;
        }
        savedSlot = -1;
        lastPos = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            // Restore tool if we moved off a block
            if (savedSlot != -1) {
                mc.player.getInventory().selectedSlot = savedSlot;
                savedSlot = -1;
            }
            lastPos = null;
            return;
        }

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        Direction face = blockHit.getSide();

        BlockState state = mc.world.getBlockState(pos);
        if (state.isAir() || state.getBlock() == Blocks.BEDROCK) return;
        float hardness = state.getHardness(mc.world, pos);
        if (hardness < 0) return;

        // Auto-tool: pick best tool from hotbar
        if (autoTool.isEnabled()) {
            int best = getBestToolSlot(state);
            if (best != -1 && best != mc.player.getInventory().selectedSlot) {
                if (savedSlot == -1) savedSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = best;
            }
        }

        // Start breaking if new block or attack button held
        if (!pos.equals(lastPos)) {
            mc.player.networkHandler.sendPacket(
                    new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, face));
            lastPos = pos;
        }

        // Send multiple stop-destroy packets scaled by speed multiplier
        long intervalMs = (long) Math.max(10, 50.0 / speedMultiplier.get());
        if (timer.hasReached(intervalMs)) {
            for (int i = 0; i < packetCount.get(); i++) {
                mc.player.networkHandler.sendPacket(
                        new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, face));
            }
            mc.interactionManager.attackBlock(pos, face);
            if (!noSway.isEnabled()) {
                mc.player.swingHand(Hand.MAIN_HAND);
            }
            timer.reset();
        }
    }

    private int getBestToolSlot(BlockState state) {
        if (mc.player == null) return -1;
        int bestSlot = -1;
        float bestSpeed = -1f;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        return bestSlot;
    }
}
