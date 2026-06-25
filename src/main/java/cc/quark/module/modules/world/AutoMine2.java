package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public class AutoMine2 extends Module {

    private final ModeSetting pattern = register(new ModeSetting(
            "Pattern", "Mining pattern to follow",
            "Line", "Line", "2x1", "3x3", "Staircase"));

    private final IntSetting length = register(new IntSetting(
            "Length", "How many blocks to mine in the pattern", 16, 1, 64));

    private final IntSetting delayMs = register(new IntSetting(
            "Delay", "Milliseconds between block breaks", 250, 50, 2000));

    private final BoolSetting swingHand = register(new BoolSetting(
            "SwingHand", "Animate hand when breaking", true));

    private final BoolSetting skipAir = register(new BoolSetting(
            "SkipAir", "Skip air blocks in the pattern", true));

    private final TimerUtil timer = new TimerUtil();
    private final List<BlockPos> mineQueue = new ArrayList<>();
    private int queueIndex = 0;

    public AutoMine2() {
        super("AutoMine2", "Auto-mines blocks in a configurable pattern", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
        buildQueue();
        queueIndex = 0;
    }

    @Override
    public void onDisable() {
        mineQueue.clear();
        queueIndex = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delayMs.get())) return;

        // Rebuild queue when exhausted or pattern changes
        if (queueIndex >= mineQueue.size()) {
            buildQueue();
            queueIndex = 0;
            if (mineQueue.isEmpty()) return;
        }

        // Find next non-air block if skipAir
        while (queueIndex < mineQueue.size()) {
            BlockPos pos = mineQueue.get(queueIndex);
            queueIndex++;

            BlockState state = mc.world.getBlockState(pos);
            if (skipAir.isEnabled() && state.isAir()) continue;
            if (state.getBlock() == Blocks.BEDROCK) continue;
            float hardness = state.getHardness(mc.world, pos);
            if (hardness < 0) continue;

            // Send start + stop destroy packets for instant-break feel
            mc.player.networkHandler.sendPacket(
                    new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.DOWN));
            mc.player.networkHandler.sendPacket(
                    new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN));
            mc.interactionManager.attackBlock(pos, Direction.DOWN);

            if (swingHand.isEnabled()) {
                mc.player.swingHand(Hand.MAIN_HAND);
            }

            timer.reset();
            return;
        }
    }

    private void buildQueue() {
        mineQueue.clear();
        if (mc.player == null) return;

        BlockPos origin = mc.player.getBlockPos();
        // Mine in the direction the player is facing (simplified to relative offsets)
        int len = length.get();

        switch (pattern.get()) {
            case "Line" -> {
                for (int i = 1; i <= len; i++) {
                    mineQueue.add(origin.add(i, 0, 0));
                }
            }
            case "2x1" -> {
                for (int i = 1; i <= len; i++) {
                    mineQueue.add(origin.add(i, 0, 0));
                    mineQueue.add(origin.add(i, 1, 0));
                }
            }
            case "3x3" -> {
                for (int i = 1; i <= len; i++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            mineQueue.add(origin.add(i, dy, dz));
                        }
                    }
                }
            }
            case "Staircase" -> {
                for (int i = 1; i <= len; i++) {
                    mineQueue.add(origin.add(i, -i, 0));
                    mineQueue.add(origin.add(i, -i + 1, 0));
                }
            }
        }
    }
}
